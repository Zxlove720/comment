package com.comment.service.impl;

import com.comment.entity.Blog;
import com.comment.mapper.BlogMapper;
import com.comment.service.IBlogService;
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
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
