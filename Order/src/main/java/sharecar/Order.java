package sharecar;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import sharecar.external.PaymentHistory;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String carNumber;
    private String carBrand;
    private String carPost;
    private String userName;
    private String cardNo;
    private String status;

    @PostPersist
    public void onPostPersist(){
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        PaymentHistory paymentHistory = new PaymentHistory();
        // mappings goes here
        System.out.println("New Order is tempted, orderId is : " + this.id);
        paymentHistory.setOrderId(this.id);
        paymentHistory.setStatus("2");
        OrderApplication.applicationContext.getBean(sharecar.external.PaymentHistoryService.class)
            .pay(paymentHistory);

    }

    @PostUpdate
    public void onPostUpdate(){
    	OrderCancelled orderCancelled = new OrderCancelled();
        System.out.println("Order is deleted, orderId is : " + this.id);
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }
    public String getCarPost() {
        return carPost;
    }

    public void setCarPost(String carPost) {
        this.carPost = carPost;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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