package com.creamakers.websystem.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creamakers.websystem.constants.CommonConst;
import com.creamakers.websystem.dao.PostCommentMapper;
import com.creamakers.websystem.dao.PostMapper;
import com.creamakers.websystem.dao.ReportPostMapper;
import com.creamakers.websystem.domain.dto.Post;
import com.creamakers.websystem.domain.dto.PostComment;
import com.creamakers.websystem.domain.dto.ReportPost;
import com.creamakers.websystem.domain.vo.ResultVo;
import com.creamakers.websystem.domain.vo.response.PostCommentResp;
import com.creamakers.websystem.domain.vo.response.PostResp;
import com.creamakers.websystem.domain.vo.response.ReportPostResp;
import com.creamakers.websystem.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.creamakers.websystem.constants.CommonConst.DATA_DELETE_FAILED_NOT_FOUND;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ReportPostMapper reportPostMapper;
    @Autowired
    private PostCommentMapper postCommentMapper;
    @Override
    public ResultVo<List<PostResp>> getAllPosts(Integer page, Integer pageSize) {
        Page<Post> pageParam = new Page<>(page,pageSize);
        Page<Post> Page = postMapper.selectPage(pageParam, new QueryWrapper<Post>().eq("is_deleted", 0));
        List<Post> records = Page.getRecords();
        List<PostResp> list = records.stream().map(this::convertToPostResp).toList();
        return ResultVo.success(list);
    }

    @Override
    public ResultVo<PostResp> getPostById(Long postId) {
        Post post = postMapper.selectById(postId);
        if(post==null) {
            return ResultVo.fail(CommonConst.BAD_REQUEST_CODE, CommonConst.BAD_USERINFO_QUERY);
        }
        PostResp postResp = convertToPostResp(post);
        return ResultVo.success(postResp);
    }

    @Override
    public ResultVo<Void> deletePostById(Long postId) {
        int i = postMapper.deleteById(postId);
        if(i<1) return ResultVo.fail(DATA_DELETE_FAILED_NOT_FOUND);
        return ResultVo.success();
    }

    @Override
    public ResultVo<List<ReportPostResp>> getAllReportedPosts(Integer page, Integer pageSize) {
        Page<ReportPost> reportPostPage = new Page<>(page, pageSize);

        QueryWrapper<ReportPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        IPage<ReportPost> reportPostsPage = reportPostMapper.selectPage(reportPostPage, queryWrapper);

        List<ReportPost> reportPosts = reportPostsPage.getRecords();
        // 如果没有未处理的举报记录，直接返回空列表
        if (reportPosts.isEmpty()) {
            return ResultVo.success(new ArrayList<>());
        }

        List<ReportPostResp> reportPostResps = reportPosts.stream().map(this::convertToReportPostResp).collect(Collectors.toList());

        return ResultVo.success(reportPostResps);
    }

    @Override
    public ResultVo<List<PostCommentResp>> getAllCommentsByPostId(Long postId, Integer page, Integer pageSize) {
        Page<PostComment> commentPage = new Page<>(page, pageSize);
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .eq("is_deleted", 0)
                .orderByDesc("create_time");

        Page<PostComment> resultPage = postCommentMapper.selectPage(commentPage, queryWrapper);

        List<PostCommentResp> commentRespList = resultPage.getRecords()
                .stream()
                .map(this::convertToPostCommentResp)
                .collect(Collectors.toList());
        return ResultVo.success(commentRespList);
    }


    private PostResp convertToPostResp(Post post) {
        PostResp postResp = new PostResp();
        BeanUtil.copyProperties(post,postResp);
        return postResp;
    }
    private ReportPostResp convertToReportPostResp(ReportPost reportPost){
        ReportPostResp reportPostResp = new ReportPostResp();
        BeanUtil.copyProperties(reportPost,reportPostResp);
        return reportPostResp;
    }
    private PostCommentResp convertToPostCommentResp(PostComment postComment){
        PostCommentResp  postCommentResp = new PostCommentResp();
        BeanUtil.copyProperties( postComment, postCommentResp);
        return  postCommentResp;
    }
}
