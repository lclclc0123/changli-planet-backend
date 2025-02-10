package com.creamakers.fresh.system.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creamakers.fresh.system.dao.FreshNewsCommentMapper;
import com.creamakers.fresh.system.domain.dto.FreshNewsComment;
import com.creamakers.fresh.system.domain.vo.ResultVo;
import com.creamakers.fresh.system.domain.vo.request.FreshNewsCommentRequest;
import com.creamakers.fresh.system.domain.vo.response.FreshNewsCommentResp;
import com.creamakers.fresh.system.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private FreshNewsCommentMapper freshNewsCommentMapper;

    /**
     * 添加评论(非回复评论)
     * @param freshNewsId 新鲜事ID
     * @param freshNewsCommentRequest 评论内容
     * @return 结果
     */
    @Override
    public ResultVo<Void> addComment(Long freshNewsId, FreshNewsCommentRequest freshNewsCommentRequest) {
        FreshNewsComment comment = new FreshNewsComment();
        comment.setNewsId(freshNewsId);
        comment.setUserId(freshNewsCommentRequest.getUserId());
        comment.setContent(freshNewsCommentRequest.getContent());
        comment.setParentId(0L);  // 一级评论的 parent_id 为 0

        int result = freshNewsCommentMapper.insert(comment);
        if (result > 0) {
            Long commentId = comment.getCommentId();
            // 更新 root 字段
            FreshNewsComment updateComment = new FreshNewsComment();
            updateComment.setCommentId(commentId);
            updateComment.setRoot(commentId);  // 设置 root 字段为评论的 ID
            freshNewsCommentMapper.updateById(updateComment);  // 使用 MyBatis-Plus 更新
            return ResultVo.success(null);
        } else {
            return ResultVo.fail("评论添加失败");
        }
    }

    /**
     * 获取新鲜事下的所有一级评论
     *
     * @param freshNewsId 新鲜事ID
     * @param page        页码
     * @param pageSize    每页大小
     * @return 一级评论列表
     */
    @Override
    public ResultVo<List<FreshNewsCommentResp>> listComments(Long freshNewsId, Integer page, Integer pageSize) {
        // 创建分页对象
        Page<FreshNewsComment> pageParam = new Page<>(page, pageSize);

        // 查询一级评论（parent_id = 0L）并按创建时间倒序排列
        Page<FreshNewsComment> pageResult = freshNewsCommentMapper.selectPage(
                pageParam,
                new QueryWrapper<FreshNewsComment>()
                        .eq("news_id", freshNewsId)  // 根据 freshNewsId 过滤
                        .eq("parent_id", 0)                // 过滤一级评论
                        .eq("is_deleted", 0)               // 确保评论没有被删除
                        .orderByDesc("liked", "create_time")        // 按点赞数正序，创建时间倒序排列
        );

        // 获取查询结果
        List<FreshNewsComment> records = pageResult.getRecords();

        // 将评论数据转换成响应对象
        List<FreshNewsCommentResp> freshNewsCommentRespList = records.stream()
                .map(freshNewsComment -> {
                    FreshNewsCommentResp resp = new FreshNewsCommentResp();
                    BeanUtils.copyProperties(freshNewsComment, resp);
                    return resp;
                })
                .collect(Collectors.toList());

        // 返回分页查询结果
        return ResultVo.success(freshNewsCommentRespList);
    }


    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public ResultVo<Void> deleteComment(Long commentId, Long userId) {
        int result = freshNewsCommentMapper.deleteByIdAndUserId(commentId, userId);
        if (result > 0) {
            return ResultVo.success(null);
        } else {
            return ResultVo.fail("评论删除失败");
        }
    }
    /*
    回复评论（添加子评论）
     */
    @Override
    public ResultVo<Void> addReply(Long commentId, FreshNewsCommentRequest freshNewsCommentRequest) {
        // 创建一个新的评论对象
        FreshNewsComment comment = new FreshNewsComment();
        comment.setNewsId(freshNewsCommentRequest.getNewsId());
        comment.setUserId(freshNewsCommentRequest.getUserId());
        comment.setContent(freshNewsCommentRequest.getContent());
        comment.setParentId(commentId);  // 设置为父评论ID

        // 查询父评论的 root 值
        FreshNewsComment parentComment = freshNewsCommentMapper.selectById(commentId);
        if (parentComment == null) {
            return ResultVo.fail("父评论不存在");
        }

        // 将子评论的 root 设置为父评论的 root
        comment.setRoot(parentComment.getRoot() != null ? parentComment.getRoot() : commentId);

        int result = freshNewsCommentMapper.insert(comment);
        if (result > 0) {
            return ResultVo.success();
        } else {
            return ResultVo.fail("评论添加失败");
        }
    }


    /**
     * 获取某一级评论评论的所有子评论
     *
     * @param commentId 评论ID
     * @param page      页码
     * @param pageSize  每页大小
     * @return 子评论列表
     */
    @Override
    public ResultVo<List<FreshNewsCommentResp>> listReplies(Long commentId, Integer page, Integer pageSize) {
        // 创建分页对象
        Page<FreshNewsComment> pageParam = new Page<>(page, pageSize);
        Page<FreshNewsComment> pageResult = freshNewsCommentMapper.selectPage(
                pageParam,
                new QueryWrapper<FreshNewsComment>()
                        .eq("root", commentId)
                        .eq("is_deleted", 0)         // 确保评论没有被删除
                        .orderByDesc("liked", "create_time")
        );

        // 获取查询结果
        List<FreshNewsComment> records = pageResult.getRecords();

        // 将评论数据转换成响应对象
        List<FreshNewsCommentResp> freshNewsCommentRespList = records.stream()
                .map(freshNewsComment -> {
                    FreshNewsCommentResp resp = new FreshNewsCommentResp();
                    BeanUtils.copyProperties(freshNewsComment, resp);
                    return resp;
                })
                .collect(Collectors.toList());
        return ResultVo.success(freshNewsCommentRespList);
    }
}
