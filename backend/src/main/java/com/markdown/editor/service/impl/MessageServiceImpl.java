package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.entity.Message;
import com.markdown.editor.mapper.MessageMapper;
import com.markdown.editor.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void sendMessage(Message message) {
        message.setIsRead(false);
        message.setCreatedAt(new Date());
        message.setUpdatedAt(new Date());
        messageMapper.insert(message);
    }

    @Override
    public List<Message> getMessagesByReceiverId(Long receiverId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<Message> getMessagesByReceiverId(Long receiverId, int page, int pageSize) {
        Page<Message> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectPage(pageObj, wrapper).getRecords();
    }

    @Override
    public long getMessagesCountByReceiverId(Long receiverId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public List<Message> getMessagesByReceiverIdAndType(Long receiverId, String type) {
        return messageMapper.selectByReceiverIdAndType(receiverId, type);
    }

    @Override
    public List<Message> getMessagesByReceiverIdAndType(Long receiverId, String type, int page, int pageSize) {
        Page<Message> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.eq("type", type);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectPage(pageObj, wrapper).getRecords();
    }

    @Override
    public long getMessagesCountByReceiverIdAndType(Long receiverId, String type) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.eq("type", type);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public List<Message> getMessagesByReceiverIdAndTypes(Long receiverId, List<String> types) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.in("type", types);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectList(wrapper);
    }

    @Override
    public List<Message> getMessagesByReceiverIdAndTypes(Long receiverId, List<String> types, int page, int pageSize) {
        Page<Message> pageObj = new Page<>(page, pageSize);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.in("type", types);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectPage(pageObj, wrapper).getRecords();
    }

    @Override
    public long getMessagesCountByReceiverIdAndTypes(Long receiverId, List<String> types) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", receiverId);
        wrapper.in("type", types);
        return messageMapper.selectCount(wrapper);
    }

    @Override
    public int getUnreadMessageCount(Long receiverId) {
        return messageMapper.countUnreadByReceiverId(receiverId);
    }

    @Override
    public void markMessagesAsRead(Long receiverId) {
        messageMapper.updateToReadByReceiverId(receiverId);
    }

    @Override
    public void deleteMessage(Long messageId) {
        messageMapper.deleteById(messageId);
    }
}
