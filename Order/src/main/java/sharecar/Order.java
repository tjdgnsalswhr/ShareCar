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
        orderPlaced.setStatus("Car is Selected, This order id is :" + this.id);
        System.out.println("Car is Selected, This order id is :" + this.id);
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();
        
        try 
        { // 피호출 서비스(결제:pay) 의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
               Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            
        } 
        catch (InterruptedException e) 
        {
                e.printStackTrace();
        }


        //Order가 생성됨에 따라, Sync/Req,Resp 방식으로 Payment를 부르는 과정
        PaymentHistory paymentHistory = new PaymentHistory();
        System.out.println("Payment is Requested, orderId is : " + this.id);
        paymentHistory.setOrderId(this.id);
        paymentHistory.setCardNo(this.cardNo);
        paymentHistory.setStatus("Payment is Requested, orderId is : " + this.id);
        OrderApplication.applicationContext.getBean(sharecar.external.PaymentHistoryService.class)
            .pay(paymentHistory);

    }

    @PreRemove
    public void onPreRemove(){
    	OrderCancelled orderCancelled = new OrderCancelled();
        orderCancelled.setStatus("Car is Canceled, This id is " + this.id);
        System.out.println("Car is Canceled, This id is " + this.id);
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
