package com.easychat.repository;

import com.easychat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByFromIdAndToIdAndMsgTypeOrderBySendTimeAsc(Long fromId, Long toId, Integer msgType);

    List<Message> findByToIdAndMsgTypeAndStatusOrderBySendTimeAsc(Long toId, Integer msgType, Integer status);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.fromId = :fromId AND m.toId = :toId AND m.msgType = :msgType")
    void updateStatusByConversation(@Param("fromId") Long fromId, @Param("toId") Long toId,
                                    @Param("msgType") Integer msgType, @Param("status") Integer status);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.msgId = :msgId")
    void updateStatusByMsgId(@Param("msgId") Long msgId, @Param("status") Integer status);
}
