package sharecar;
/**/
import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        ReservationAccepted reservationAccepted = new ReservationAccepted();
        System.out.println("Reservation is created, orderId is : " + this.orderId);
        BeanUtils.copyProperties(this, reservationAccepted);
        reservationAccepted.publishAfterCommit();
        
    }

    @PreRemove
    public void onPreRemove() {
    	ReservationCanceled reservationCanceled = new ReservationCanceled();
        System.out.println("Reservation is Canceled, This id is " + this.id);
        BeanUtils.copyProperties(this, reservationCanceled);
        reservationCanceled.publishAfterCommit();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
