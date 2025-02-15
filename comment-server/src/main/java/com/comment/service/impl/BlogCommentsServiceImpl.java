package com.comment.service.impl;

import com.comment.entity.BlogComments;
import com.comment.mapper.BlogCommentsMapper;
import com.comment.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wzb
 * @since 2025-2-12
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
