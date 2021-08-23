package sharecar;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="PaymentHistory_table")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String cardNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        PaymentApproved paymentApproved = new PaymentApproved();
        paymentApproved.setStatus("Payment is Approved");
        System.out.println("Payment is approved, orderId is : " + this.orderId);
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();

    }
    @PostUpdate
    public void onPostUpdate() {
    	PaymentCanceled paymentCanceled = new PaymentCanceled();
        System.out.println("Payment is canceled, orderId is : " + this.orderId);
        paymentCanceled.setStatus("Payment canceled");
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publishAfterCommit();
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
    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}