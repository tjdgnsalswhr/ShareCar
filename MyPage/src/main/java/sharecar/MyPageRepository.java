package sharecar;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    List<MyPage> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}