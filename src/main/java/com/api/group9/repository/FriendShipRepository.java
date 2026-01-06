package com.api.group9.repository;

import com.api.group9.model.FriendShip;
import com.api.group9.model.User;
import com.api.group9.enums.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendShipRepository extends JpaRepository<FriendShip, Long> {

       // 1. Check xem 2 đứa này có quan hệ gì chưa (Bất kể ai gửi trước)
       // Để tránh việc A gửi cho B, xong B lại gửi ngược lại cho A -> Loạn DB
       @Query("SELECT f FROM FriendShip f WHERE " +
                     "(f.sender = :user1 AND f.receiver = :user2) OR " +
                     "(f.sender = :user2 AND f.receiver = :user1)")
       Optional<FriendShip> findRelationship(@Param("user1") User user1, @Param("user2") User user2);

       // 2. Lấy danh sách bạn bè (Status = ACCEPTED)
       // Lấy tất cả bản ghi mà mình dính dáng (là sender hoặc receiver) VÀ đã ACCEPT
       @Query("SELECT f FROM FriendShip f WHERE " +
                     "f.status = 'ACCEPTED' AND " +
                     "(f.sender = :me OR f.receiver = :me)")
       List<FriendShip> findAllFriends(@Param("me") User me);

       // 3. Lấy danh sách lời mời ĐANG CHỜ mình duyệt
       // Mình phải là Receiver và Status là PENDING
       List<FriendShip> findByReceiverAndStatus(User receiver, FriendStatus status);

       @Query("SELECT COUNT(f) FROM FriendShip f WHERE (f.sender.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
       long countFriends(@Param("userId") Long userId);

       @Query("SELECT f.receiver.id FROM FriendShip f WHERE f.sender.id = :userId AND f.status = 'ACCEPTED' " +
                     "UNION " +
                     "SELECT f.sender.id FROM FriendShip f WHERE f.receiver.id = :userId AND f.status = 'ACCEPTED'")
       List<Long> findAllFriendIds(@Param("userId") Long userId);
       
}