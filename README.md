# ShareCar

Socar나 Green Car와 같은 카셰어링을 간단히 따라해보는 서비스입니다.

# Table of contents

- [공유차량예약](#---)
  - [서비스 시나리오](#시나리오)
  - [분석/설계](#분석-설계)
  - [코드구현 및 실행결과](#코드구현-및-실행결과)
    - [DDD 의 적용](#ddd-의-적용)
    - [코드 내용](#코드-내용)
    - [Saga Pattern](#Local에서의-코드-실행-결과)
    - [Polyglot (Check-Point)](#Polyglot-(Check-Point))
    - [CQRS - MyPage (Check-Point)](#CQRS-MyPage-(Check-Point))
    - [Correlation (Check-Point)](#Correlation-(Check-Point))
    - [동기식 호출 - Req/Resp (Check-Point)](#동기식-호출 - Req/Resp-(Check-Point))
    - [Async 호출 - Pub/Sub](#Async 호출 - Pub/Sub)
    - [API Gateway (Check-Point)](#API-Gateway-(Check-Point))
  - [운영](#운영)
    - [CI/CD (Check-Point)](#CI/CD-(Check-Point))
    - [Circuit Breaker (Check Point)](#Circuit-Breaker-(Check-Point))
    - [Autoscale : HPA (Check-Point)](#Autoscale-HPA-(Check-Point))
    - [Readiness Probe : Zero-downtime deploy (Check-Point)](#Readiness-Probe-:-Zero-downtime-deploy (Check-Point))
    - [Liveness Probe : Self-healing (Check-Point)](#Liveness-Probe-:-Self-healing-(Check-Point))
    - [Configmap (Check-Point) / Secret](#Configmap-(Check-Point))


<br>

## 서비스 시나리오

공유차량 예약 시스템에서 요구하는 기능/비기능 요구사항은 다음과 같습니다. 사용자가 차량 예약과 함께 결제를 진행하고 나면 자동으로 승인되는 시스템입니다. 이 과정에 대해서 고객은 진행 상황과 내역을 확인할 수 있습니다. 

#### 기능적 요구사항

1. 사용자는 원하는 차량을 선택한다. (사용할 수 없는 시간대는 선택할 수 없도록 되어있다.) 
2. 사용자는 결제를 진행한다.
3. 예약이 신청되면 시스템에서는 예약 정보가 생성된다.
5. 사용자는 차량을 취소할 수 있다.
6. 차량이 취소되면 예약 정보도 취소 처리가 된다.
7. 예약이 취소되면 결제도 취소가 된다.
8. 고객이 예약 정보를 중간 조회할 수 있다.

#### 비기능적 요구사항

1. 트랜잭션
   - 차량이 주문되고 결제가 완료되어야만 예약 정보가 아예 생성된다.
   - 결제가 되지 않은 예약건은, 아예 예약 정보가 생성되지 않아야 한다. `Sync 호출`

2. 장애격리
   - 시스템 쪽에서 차량 관리 기능이 수행되지 않더라도, 차량 예약은 항상 진행될 수 있어야 한다. `Pub/Sub` `Async 호출`
   - 결제 시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시후에 결제하도록 한다.
     (장애처리 : Circuit Breaker, fallback)

3. 성능
   - 고객이 예약 확인 상태를 MyPage에서 확인할 수 있어야 한다. `CQRS`
   


# 분석 설계

## Event Storming 결과


#### 이벤트 도출


![image](https://user-images.githubusercontent.com/32426312/130356854-2e6eb5ea-e14b-45b9-8223-e3319ed6df8d.png)


#### 부적격 이벤트 탈락

- 차량검색됨과 예약정보 조회됨는, UI적으로 발생하는 이벤트지 업무적인 이벤트가 아니므로 제외함. 

![image](https://user-images.githubusercontent.com/32426312/130359864-33e8ef85-792d-4206-8625-2d28b0952efe.png)

- 후에 코드 변환을 위해 영어로 전환.

![image](https://user-images.githubusercontent.com/32426312/130784377-e0475ccd-3508-4976-bae2-0f466bb97e95.png)

- 처음에 Order가 아닌 Car라는 단어를 사용하려고 했으나, 보통 이런 경우 공용으로 쓰이는 단어인 Order를 사용하는게 좋다고 배워 Order로 지칭.



#### Actor, Command

![image](https://user-images.githubusercontent.com/32426312/130785123-8dbbc1c3-5919-4885-95d9-c74051149309.png)

#### Aggregate

![image](https://user-images.githubusercontent.com/32426312/130357633-21e52a5b-19df-4f2e-8acb-5fc5780e918a.png)

#### Bounded Context

![image](https://user-images.githubusercontent.com/32426312/130357731-a12f245a-6585-4caf-86e5-f957c077b150.png)

#### Policy

![image](https://user-images.githubusercontent.com/32426312/130358246-bea64aa3-abb7-48d0-b282-eef3f863773e.png)


#### 최종 결과

![image](https://user-images.githubusercontent.com/32426312/130359638-4d4fc64d-9fdb-4886-88d5-48464f9df22e.png)



### 기능 요구사항을 커버하는지 검증
1. 사용자는 원하는 차량을 선택한다. (사용할 수 없는 시간대는 선택할 수 없도록 되어있다.) (O)
2. 사용자는 결제를 진행한다. (O)
3. 예약이 신청되면 시스템에서는 예약 정보가 생성된다. (O)
5. 사용자는 차량을 취소할 수 있다. (O)
6. 차량이 취소되면 예약 정보도 취소 처리가 된다. (O)
7. 예약이 취소되면 결제도 취소가 된다. (O)
8. 고객이 예약 정보를 중간 조회할 수 있다. (O)


### 비기능 요구사항을 커버하는지 검증

1. 트랜잭션 
   - 결제가 되지 않은 예약건은, 아예 예약 정보가 생성되지 않아야 한다. `Sync 호출`(O)
   - Request-Response 방식 처리 (OrderPlaced -> pay : 결제를 거치지 않고서는 Reservation이 생성되지 않는다.)

2. 장애격리
   - 다른쪽에 문제가 생기더라도, 차량 예약은 항상 진행될 수 있어야 한다. (O)
   - `(Pub/Sub)` `Async 호출` 방식으로 트랜잭션 처리.

3. 성능
   - 고객이 예약 확인 상태를 마이페이지에서 확인할 수 있어야 한다. `CQRS`



# 코드구현 및 실행결과

## DDD의 적용

- 코드 구현의 경우, 위에서 생성한 DDD를 기반으로 코드 자동생성을 한 후 에러를 수정하였다.
- 그 뒤 필요한 비즈니스 로직을 내부에 추가하였다.
- 크게 Order, Payment, Reservation, MyPage가 있지만, 모든 코드를 여기에 담는 것에는 한계가 있으므로 Payment를 예시로 첨부한다.
- Payment를 첨부하는 이유는, Order와 Req/Resp 방식의 Sync 통신이 구현됨과 동시에 Reservation과 Pub/Sub 방식의 Async 통신도 담고 있기 때문이다.

 
## 코드 내용


### Aggregate (Payment Service) - PaymentHistory.java

```java
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
        paymentApproved.setOrderId(this.id);
        paymentApproved.setStatus("Payment is Approved");
        paymentApproved.setCardNo(this.cardNo);
        System.out.println("Payment is approved, orderId is : " + this.orderId);
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();

    }
    @PreRemove
    public void onPreRemove() {
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
```

### Policy (Payment Service) - PolicyHandler.java

```java
package sharecar;

import sharecar.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired 
    PaymentHistoryRepository paymentHistoryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReservationCanceled_CancelPayment(@Payload ReservationCanceled reservationCanceled){

        if(reservationCanceled.validate()){

            System.out.println("\n\n##### listener CancelPayment : " + reservationCanceled.toJson() + "\n\n");
            PaymentHistory payment = paymentHistoryRepository.findByOrderId(reservationCanceled.getOrderId());
            payment.setStatus("PaymentCancelled!");
            paymentHistoryRepository.save(payment);
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}
}
```

### Repository (Payment Service) - PaymentHistoryRepository.java

```java
package sharecar;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="paymentHistories", path="paymentHistories")
public interface PaymentHistoryRepository extends PagingAndSortingRepository<PaymentHistory, Long>{
    PaymentHistory findByOrderId(Long orderId);
}

```


## Saga Pattern - Local에서의 코드 실행 (Check Point)


### 각 마이크로서비스 실행

 - 먼저 구현한 각 서비스를 다음과 같이 명령어로 실행한다. 
 - (Order : 8081, Payment : 8082, Reservation : 8083, MyPage : 8084)


#### Order

![image](https://user-images.githubusercontent.com/32426312/131766498-bbb50067-13fd-4741-9fc4-076564edde59.png)

&nbsp;

#### Payment

![image](https://user-images.githubusercontent.com/32426312/131766555-e62b7c5c-c38a-4422-a894-3c4bd2c8ab2d.png)

&nbsp;

#### Reservation>

![image](https://user-images.githubusercontent.com/32426312/131766674-241c7111-47ae-454f-94d6-cea19e5aee6e.png)

&nbsp;


### REST API 의 테스트  

#### Order 서비스에서 주문처리 (차량 신청 처리)

- 다음의 명령어를 사용하여 두 개의 차량 주문을 넣는다.

```java
http localhost:8081/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청
http localhost:8081/orders carNumber=101가1231 carBrand=아반떼 carPost=우림빌딩 userName=Park status=차량신청
```

- 실행결과  

![image](https://user-images.githubusercontent.com/32426312/131767893-d838fdfc-027f-460d-b2ca-3001db681bdc.png)

![image](https://user-images.githubusercontent.com/32426312/131767920-fc538dd3-0428-4734-a904-b65b643c66c9.png)
  
&nbsp;

#### Payment 서비스에서 조회

- Order에서 Payment로 Sync, Req/Resp 방식으로 호출하므로, Order에서 주문이 생성되면 Payment에서도 조회가 가능해야한다.

```java
http GET localhost:8082/paymentHistories
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131768274-0f47af35-2586-48f7-b514-d3d533ac2d5b.png)
	
- 앞서 생성한 두개의 orderId가 조회되고 있다.

&nbsp;

#### Reservation 서비스에서 조회

- 결제가 진행되면 Reservation이 생성된다.

```java
http GET localhost:8083/reservations
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131768706-a49dbd1c-c0ba-48a9-b79f-12964632b748.png)




## Polyglot (Check-Point)

- MSA의 가장 장점 중 하나는, 마이크로서비스 별로 Language나 DB가 달라도 된다는 것이다.
- Polyglot을 잘 만족하는지 확인하기 위해서, Order 서비스의 DB를 H2에서 HSQLDB로 변경한다.

&nbsp;
	
### 변경전

![image](https://user-images.githubusercontent.com/32426312/131770573-82cec61b-b01c-4480-9657-657f54f6e635.png)

&nbsp;
	
### 변경후

![image](https://user-images.githubusercontent.com/32426312/131770693-9956d2a8-794f-4064-b534-bcf927d41bbf.png)

- 잘 되는지 확인하기 위해 Order 및 다른 서비스들을 재기동 한 후, 앞에서 행했던 REST TEST를 진행한다.


#### Order 서비스에서 주문처리 (차량 신청 처리) 

```java
http localhost:8081/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청_Polyglot
http localhost:8081/orders carNumber=101가1231 carBrand=아반떼 carPost=우림빌딩 userName=Park status=차량신청_Polyglot
```

- 실행결과  

![image](https://user-images.githubusercontent.com/32426312/131771112-0d6a3780-34a5-4365-8d12-580cac164eb6.png)

![image](https://user-images.githubusercontent.com/32426312/131771156-71eafe89-88b1-4272-8e0e-099ff32daf7b.png)
  
&nbsp;

#### Payment 서비스에서 조회

```java
http GET localhost:8082/paymentHistories
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131771226-1efb978e-74d3-41df-a673-135b915b1a3b.png)

	
&nbsp;

#### Reservation 서비스에서 조회


```java
http GET localhost:8083/reservations
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131771315-3180b019-1449-48e2-a7af-5df47a747f21.png)

&nbsp;
	


## CQRS - MyPage (Check-Point)


사용자가 예약정보를 한 눈에 볼 수 있는 MyPage를 구현 한다.(CQRS)

### MyPage 생성

![image](https://user-images.githubusercontent.com/32426312/130360123-c5069bb7-3478-4bc5-ac6c-08b198e769f3.png)

	-> MyPage 에서는 예약정보를 볼 수 있어야 하므로 모든 변수를 주입해준다.


![image](https://user-images.githubusercontent.com/32426312/130360160-91d25592-6e6c-4efd-a741-5a7485e070d5.png)

	-> MyPage는 기본적으로 Order가 생성될때 같이 생성되도록 한다.


![image](https://user-images.githubusercontent.com/32426312/130360195-3f7f5f92-16e1-4992-b8c6-a22bec86a80d.png)

![image](https://user-images.githubusercontent.com/32426312/130360245-cf47f53e-c444-4be9-9712-a5d00845a2cf.png)


	-> status의 경우 1은 order만 진행된 상태, 2는 결제까지 진행된 상태, 3은 예약이 완료된 상태이다.
	-> 결제완료, 결제취소, 예약확정, 예약취소의 경우 MyPage의 status를 변화시킨다.


![image](https://user-images.githubusercontent.com/32426312/130360264-976aa30b-efbe-4a20-b649-5c35a641a918.png)

 	-> Order가 삭제될 때, 관련된 MyPage도 삭제된다.

&nbsp;

### MyPage 코드 자동 생성


#### MyPage.java

```java
package sharecar;

import javax.persistence.*;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String cardNumber;
        private String cardBrand;
        private String cardPost;
        private String userName;
        private String cardNo;
        private String status;
        private Long orderId;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }
        public String getCardBrand() {
            return cardBrand;
        }

        public void setCardBrand(String cardBrand) {
            this.cardBrand = cardBrand;
        }
        public String getCardPost() {
            return cardPost;
        }

        public void setCardPost(String cardPost) {
            this.cardPost = cardPost;
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
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

}

```

#### MyPageViewHandler.java

```java
package sharecar;

import sharecar.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPlaced_then_CREATE_1 (@Payload OrderPlaced orderPlaced) {
        try {

            if (!orderPlaced.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setCardNumber(orderPlaced.getCarNumber());
            myPage.setCardBrand(orderPlaced.getCarBrand());
            myPage.setCardPost(orderPlaced.getCarPost());
            myPage.setUserName(orderPlaced.getUserName());
            myPage.setCardNo(orderPlaced.getCardNo());
            myPage.setOrderId(orderPlaced.getId());
            myPage.setStatus("차량 신청됨");
            // view 레파지 토리에 save
            myPageRepository.save(myPage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_UPDATE_1(@Payload PaymentApproved paymentApproved) {
        try {
            if (!paymentApproved.validate()) return;
                // view 객체 조회

                    MyPage myPage = myPageRepository.findByOrderId(paymentApproved.getOrderId());
                    //for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setCardNo(paymentApproved.getCardNo());
                    myPage.setStatus("차량 금액 결제됨");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                //}

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationAccepted_then_UPDATE_2(@Payload ReservationAccepted reservationAccepted) {
        try {
            if (!reservationAccepted.validate()) return;
                // view 객체 조회

                    ///List<MyPage> myPageList = myPageRepository.findByOrderId(reservationAccepted.getOrderId());
                    MyPage myPage = myPageRepository.findByOrderId(reservationAccepted.getOrderId());
                    //for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus("예약 완료됨");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                //}

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_3(@Payload PaymentCanceled paymentCanceled) {
        try {
            if (!paymentCanceled.validate()) return;
                // view 객체 조회

                    ///List<MyPage> myPageList = myPageRepository.findByOrderId(reservationAccepted.getOrderId());
                    MyPage myPage = myPageRepository.findByOrderId(paymentCanceled.getOrderId());
                    //for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus("결제 취소됨");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                //}

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReservationCanceled_then_UPDATE_4(@Payload ReservationCanceled reservationCanceled) {
        try {
            if (!reservationCanceled.validate()) return;
               // view 객체 조회

                    ///List<MyPage> myPageList = myPageRepository.findByOrderId(reservationAccepted.getOrderId());
                    MyPage myPage = myPageRepository.findByOrderId(reservationCanceled.getOrderId());
                    //for(MyPage myPage : myPageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    myPage.setStatus("예약 취소됨");
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
                //}

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCancelled_then_DELETE_1(@Payload OrderCancelled orderCancelled) {
        try {
            if (!orderCancelled.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            myPageRepository.deleteByOrderId(orderCancelled.getId());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

```

#### PolicyHandler.java

```java
package sharecar;

import sharecar.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{

    @Autowired
    MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_CancelMyPage(@Payload OrderCancelled orderCancelled){

        if(orderCancelled.validate()){
            System.out.println("##### MyPage OrderCanceled listener  : " + orderCancelled.toJson());
            MyPage myPage = myPageRepository.findByOrderId(orderCancelled.getId());
	        //reserve.setStatus("Cancelled!");
            myPageRepository.delete(myPage);
        }
    }
}
```
	
#### MyPageRepository.java

```java
package sharecar;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    MyPage findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
```


### MyPage 실행

![image](https://user-images.githubusercontent.com/32426312/131773620-88b505a1-364a-4804-98b4-c799ce78e138.png)



### MyPage 테스트


- 모든 마이크로서비스를 재기동 후 주문을 다시 넣어준다.
- 그 후 MyPage로 확인한다.

```bash
http localhost:8081/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청_Polyglot
http localhost:8081/orders carNumber=101가1231 carBrand=아반떼 carPost=우림빌딩 userName=Park status=차량신청_Polyglot

http localhost:8084/myPages
```

![image](https://user-images.githubusercontent.com/32426312/131780363-89ffa6a2-4e73-440a-a7b9-6f39af9cc807.png)

- 주문을 넣고 결제 및 예약이 완료되어 MyPage 상으로도 체크할 수 있는걸 확인할 수 있다.

&nbsp;


## Correlation (Check-Point)

- Correation을 확인할 수 있는 방법은 다음과 같다.
- 현재 구조상, 
1. 만약 order 하나를 지운다면 Pub/Sub 호출로 인해 관련된 예약정보가 Cancel 되고,
2. 예약정보의 Pub/Sub 호출로 인해 다시 관련된 Payment가 Cancel 된다.
3. Mypage 또한 Order가 지워지면 삭제되므로, 이 또한 확인이 가능해야 한다.

### Test

- 먼저 order를 조회해본다.

```bash
http localhost:8081/orders
```

![image](https://user-images.githubusercontent.com/32426312/131780715-8689cb89-80a5-4b26-87f0-fa6db4c5bdbd.png)

&nbsp;

- 그 후 orderId가 1인 orders를 지운다.

```bash
http DELETE localhost:8081/orders/1
```

![image](https://user-images.githubusercontent.com/32426312/131780832-a7052d48-edf2-4476-afa0-59d3b8da18ff.png)

&nbsp;

- 잘 지워졌는지 확인한다.

```bash
http localhost:8081/orders
```

![image](https://user-images.githubusercontent.com/32426312/131780915-c285cb14-7ede-4067-991e-847a26cc563a.png)

&nbsp;

- 그 후 Reservation 서비스에도 잘 Cancel 됬는지 확인한다.
- 아래 사진을 통해, 이전에는 있었던 orderId=1 에 대한 정보가 사라졌음을 확인할 수 있다.

```bash
http localhost:8083/reservations
```

![image](https://user-images.githubusercontent.com/32426312/131781015-9de25e33-bb26-46c2-ba21-77fd9f4cde88.png)

&nbsp;

- 이제 예약이 cancel 됬으므로 관련된 결제가 취소됬는지 확인한다.
- 아래 사진을 통해, orderId=1 에 대한 Payment 상태가 Cancelled로 갱신된걸 확인할 수 있다.

```bash
http localhost:8082/paymentHistories
```

![image](https://user-images.githubusercontent.com/32426312/131781191-f0670547-37b2-41ce-80b2-adf759be493c.png)


&nbsp;

- 마지막으로 해당 정보가 MyPage에도 잘 보이는지 확인해본다.
- 아래 사진을 통해, orderId=1에 대한 MyPage가 삭제된걸 확인할 수 있다.

```bash
http localhost:8084/myPages
```

![image](https://user-images.githubusercontent.com/32426312/131781395-57bb0e9e-d902-429b-8c62-8d57e86b4f45.png)

&nbsp;


## 동기식 호출 - Req/Resp (Check-Point)

- 분석단계에서의 조건 중 하나로 트랜잭션을 적용하기 위해 주문(order)->결제(pay) 간의 호출은 Sync 호출을 사용하기로 했다.

### 구현

#### Order.java (중에서..)

```java

    @PostPersist
    public void onPostPersist(){


        OrderPlaced orderPlaced = new OrderPlaced();
        orderPlaced.setStatus("Car is Selected, This order id is :" + this.id);
        System.out.println("Car is Selected, This order id is :" + this.id);
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();


        //Order가 생성됨에 따라, Sync/Req,Resp 방식으로 Payment를 부르는 과정
        PaymentHistory paymentHistory = new PaymentHistory();
        System.out.println("Payment is Requested, orderId is : " + this.id);
        paymentHistory.setOrderId(this.id);
        paymentHistory.setCardNo(this.cardNo);
        paymentHistory.setStatus("Payment is Requested, orderId is : " + this.id);
        OrderApplication.applicationContext.getBean(sharecar.external.PaymentHistoryService.class)
            .pay(paymentHistory);

    }
    
```

- 위와 같은 코드로, Order가 생성될때 필요한 Parameter들을 담아 External 객체 안에 있는 pay 함수를 호출한다.


#### PaymentHistoryService.java (중에서..)

```java
package sharecar.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="Payment", url="${api.payment.url}")
public interface PaymentHistoryService {
    @RequestMapping(method= RequestMethod.POST, path="/paymentHistories")
    public void pay(@RequestBody PaymentHistory paymentHistory);

}

```

- Order 서비스 프로젝트 폴더에 External 형식으로 Import 된 PaymentHistoryService에는 위와 같이 pay 함수가 구현되었다.
- 동기식 호출을 위해 FeignClient를 사용하였으며, 해당 url을 통해 Payment 서비스로 Request를 날리고 Response를 받을 수 있다.


### 잘 되는지 TEST

- Order를 생성했을 때 Payment쪽에도 정보가 잘 넘어가는 것은 위에 REST API TEST에서 확인할 수 있는 내용으로, Sync 호출 (Req/Resp)이 잘 되고 있는 것에 대한 증명이다.



### 트랙잭션 TEST

- 그렇다면 의도한대로, 결제 시스템에서 장애가 나면 트랜잭션이 완료되지 못하는지 확인해본다.
- Payment 서비스를 잠시 내린 후에 REST API 테스트를 진행한다.

```java
http localhost:8081/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청
```

![image](https://user-images.githubusercontent.com/32426312/131782759-066f234c-9614-4056-8bd8-8b2b0495974e.png)

&nbsp;

- 위와 같이 Payment 서비스가 동작하지 않자, 주문 자체가 안되는 것을 확인할 수 있다.

- 이제 다시 Payment 서비스를 재기동해본다.
- 그 후 다시 실행한다.


```java
http localhost:8081/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청
```

![image](https://user-images.githubusercontent.com/32426312/131782904-8d0b945b-23ac-49f5-aad0-ddf0593a98f6.png)


- 그러자 이번에는 잘 성공하는 것을 확인할 수 있다.

&nbsp;


## Async 호출 - Pub/Sub

- 비동기식 호출의 경우, 현재 Pament->Reservation, Order->Reservation, Reservation->Payment 가 Pub/Sub 방식으로 구현되어있다.
- 위에서 Saga Pattern을 테스트 하기 위해 쭉 흐름대로 해봤을때 모두 문제 없이 됬으므로, 비동기식 호출 또한 잘 되고 있다는 것을 확인할 수 있다.



## API Gateway (Check-Point)

- 이제 API Gateway 를 적용하여 모든 마이크로서비스의 진입점을 통일시킨다.


### Gateway 구현 (Application.yml 파일 중에서..)

```java

server:
  port: 8080

---
spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: Order
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: Payment
          uri: http://localhost:8082
          predicates:
            - Path=/paymentHistories/** 
        - id: Reservation
          uri: http://localhost:8083
          predicates:
            - Path=/reservations/** 
        - id: MyPage
          uri: http://localhost:8084
          predicates:
            - Path= /myPages/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: Order
          uri: http://Order:8080
          predicates:
            - Path=/orders/** 
        - id: Payment
          uri: http://Payment:8080
          predicates:
            - Path=/paymentHistories/** 
        - id: Reservation
          uri: http://Reservation:8080
          predicates:
            - Path=/reservations/** 
        - id: MyPage
          uri: http://MyPage:8080
          predicates:
            - Path= /myPages/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
---
```

- 위 코드에서 API Gateway는 8080 포트로 사용하였고, 각 서비스로 접근할 수 있게 잘 지정해주었다.



### Gateway 실행 (8080 포트)

![image](https://user-images.githubusercontent.com/32426312/131783700-e6163a8c-0b3a-4c3b-9567-b34f791421ca.png)


### Api Gateway 만을 사용한 API TEST

#### Gateway 에서 주문처리 (차량 신청 처리)

- 다음의 명령어를 사용하여 두 개의 차량 주문을 넣는다.

```bash
http localhost:8080/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청
http localhost:8080/orders carNumber=101가1231 carBrand=아반떼 carPost=우림빌딩 userName=Park status=차량신청
```

- 실행결과  

![image](https://user-images.githubusercontent.com/32426312/131784128-d4bb0229-925b-4b13-bddc-6be746dab212.png)

![image](https://user-images.githubusercontent.com/32426312/131784156-aa110d0c-f602-4f30-b6fa-e04a1e8fb335.png)
  
&nbsp;

#### Gateway 에서 Payment 조회

```bash
http GET localhost:8080/paymentHistories
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131784263-658d065f-c6aa-4a49-98e2-2aa7ec4f8e21.png)
	
&nbsp;

#### Gateway 에서 Reservation 조회

```bash
http GET localhost:8080/reservations
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131784378-118308ba-f909-4120-892b-4b10bd8d1535.png)

&nbsp;

#### Gateway 에서 MyPage 조회

```bash
http GET localhost:8080/myPages
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131784474-b4425e1e-4ec8-45f3-9472-4cc5dbb36355.png)

&nbsp;
&nbsp;


# 운영

## EKS 생성 및 설정

### EKS 생성

-  다음과 같은 명령어로 EKS를 생성한다.

```bash
eksctl create cluster --name (Cluster-Name) --version 1.19 --nodegroup-name standard-workers --node-type t3.medium --nodes 4 --nodes-min 1 --nodes-max 4
```

- 시간이 꽤 흐르면 자기가 알아서 생성한다.


### 엑세스 키 생성

- AWS IAM으로 접근한 뒤, 엑세스 키를 생성한다.
- 이때 Public Key와 Private Key는 자신만 알 수 있는 곳에 저장해둔다.


### AWS Configure 주입하기

- 터미널에서 aws configure을 실행한뒤 다음의 네가지 항목을 입력한다.
- AWS Access Key ID - IAM에서 생성한 퍼블릭 엑세스 키
- AWS Secret Access Key - IAM에서 생성한 프라이빗 엑세스 키
- Default region name - 본인이 사용하고 있는 Region 코드
- Default output format - json


### EKS에 Zookeeper & Kafka 설치 및 실행

- 다음의 명령어를 순차적으로 실행한다.

```bash
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
kubectl --namespace kube-system create sa tiller      # helm 의 설치관리자를 위한 시스템 사용자 생성
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
helm repo add incubator https://charts.helm.sh/incubator
helm repo update
kubectl create ns kafka
helm install my-kafka --namespace kafka incubator/kafka
```

![image](https://user-images.githubusercontent.com/32426312/131796374-21c10eea-c64d-4366-9913-c914cdf60de3.png)

![image](https://user-images.githubusercontent.com/32426312/131796483-ecb04c70-76a2-49ed-bebd-916f43df2774.png)


- kafka가 잘 설치되어 있는지 확인한다.

![image](https://user-images.githubusercontent.com/32426312/131796787-575d38ca-e534-4878-a4cb-d47687c5f4b3.png)


### EKS에 Metric 서버 설치

- 다음의 명령어로 쿠버네티스 안에 Metric Server를 설치한다.

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.5.0/components.yaml
```


## CI/CD (Check-Point)

### 마이크로 서비스 별로 Git Repo 준비.

- CI/CD를 설정하기 위해서는, 마이크로서비스 별로 Repository가 존재해야한다.
- Github에서 Order, Payment, Reservation, MyPage, Gateway에 대한 Git Repo를 생성하고 코드를 올린다.

![image](https://user-images.githubusercontent.com/32426312/131786474-f3571057-aff6-4044-af3a-17e29409f68e.png)


### AWS ECR에 마이크로 서비스 별로 Resistry 준비

- 각 마이크로서비스별 Repository에 연결될 ECR 저장소도 필요하다.

![image](https://user-images.githubusercontent.com/32426312/131808134-519e970c-9485-45c5-bcd5-12ac83f44fa2.png)


### 각 Git Repo 안에 buildspec.yml 파일 준비

- Git Repo -> AWS ECR -> AWS CodeBuild -> AWS EKS 를 연결하는데 필요한 Buildspec.yml파일을 준비합니다.
- Order에 설정한 buildspec 내용을 예시로 첨부합니다.
- PROJECT_NAME은 해당 Repo와 연결되야하는 ECR 이름을 입력합니다.

```yml
version: 0.2
##
env:
  variables:
    _PROJECT_NAME: "sharecar-order"
#
phases:
  install:
    runtime-versions:
      java: corretto11
      docker: 18
    commands:
      - echo install kubectl
      - curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl
      - chmod +x ./kubectl
      - mv ./kubectl /usr/local/bin/kubectl
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - echo $_PROJECT_NAME
      - echo $AWS_ACCOUNT_ID
      - echo $AWS_DEFAULT_REGION
      - echo $CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo start command
      - $(aws ecr get-login --no-include-email --region $AWS_DEFAULT_REGION)
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Docker image...
      - mvn package -Dmaven.test.skip=true
      - docker build -t $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION  .
  post_build:
    commands:
      - echo Pushing the Docker image...
      - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo connect kubectl
      - kubectl config set-cluster k8s --server="$KUBE_URL" --insecure-skip-tls-verify=true
      - kubectl config set-credentials admin --token="$KUBE_TOKEN"
      - kubectl config set-context default --cluster=k8s --user=admin
      - kubectl config use-context default
      - |
          cat <<EOF | kubectl apply -f -
          apiVersion: v1
          kind: Service
          metadata:
            name: $_PROJECT_NAME
            labels:
              app: $_PROJECT_NAME
          spec:
            ports:
              - port: 8080
                targetPort: 8080
            selector:
              app: $_PROJECT_NAME
          EOF
      - |
          cat  <<EOF | kubectl apply -f -
          apiVersion: apps/v1
          kind: Deployment
          metadata:
            name: $_PROJECT_NAME
            labels:
              app: $_PROJECT_NAME
          spec:
            replicas: 1
            selector:
              matchLabels:
                app: $_PROJECT_NAME
            template:
              metadata:
                labels:
                  app: $_PROJECT_NAME
              spec:
                containers:
                  - name: $_PROJECT_NAME
                    image: $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$_PROJECT_NAME:$CODEBUILD_RESOLVED_SOURCE_VERSION
                    ports:
                      - containerPort: 8080
                    readinessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 10
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 10
                    livenessProbe:
                      httpGet:
                        path: /actuator/health
                        port: 8080
                      initialDelaySeconds: 120
                      timeoutSeconds: 2
                      periodSeconds: 5
                      failureThreshold: 5
          EOF
#cache:
#  paths:
#    - '/root/.m2/**/*'
```

### 환경변수 준비

- AWS CodeBuild를 설정하기 위해서는 환경변수가 필요합니다.
- AWS REGION : 현재 사용하고 있는 지역
- AWS_ACCOUNT_ID : 본인의 ACCOUNT
- KUBE_URL : EKS의 엔드포인트
- KUBE_TOKEN : EKS 토큰
- EKS 토큰의 경우 다음의 명령어로 가져올 수 있다.
- kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep eks-admin | awk '{print $1}')


### AWS Codebuild 생성

- 각 서비스 별로 Codebuild를 생성한다.

![image](https://user-images.githubusercontent.com/32426312/131793572-6aaa4d34-5d86-47b4-b9dc-358ce44d8832.png)


#### IAM 수정

- CodeBuild에서 ECR에 접근하기 위해서는 권한을 추가해주어야 합니다.
- 각 Codebuild에서 빌드세부정보->서비스 역할 -> 인라인정책 추가 -> JSON을 선택한 뒤 다음과 같이 추가합니다.

![image](https://user-images.githubusercontent.com/32426312/131794606-07f70249-f9e9-4363-93fc-90cd0ce5ffe8.png)


### 배포

#### Codebuild를 사용하여 쿠버네티스에 배포

- 이제 모든 준비가 끝났으므로, 각 Git Repo에서 Trigger를 주어 빌드를 실행한다.
- 각주 한줄을 추가한 뒤, Commit& Changes 를 클릭한다.

![image](https://user-images.githubusercontent.com/32426312/131826222-dd7a34a6-2112-4aa9-b94d-cf204cdc28e3.png)

- AWS Codebuild가 연결된 Github에서 소스를 가져와 빌드하고 쿠버네티스에 배포를 시작한다.
- 시간이 조금 걸리므로 잠시 기다린다.

![image](https://user-images.githubusercontent.com/32426312/131827826-57c477d6-7529-44cb-a8b9-dc80486b976a.png)

- 위와 같이 성공 메세지가 뜨면 배포가 성공한 것이다.


#### 쿠버네티스에서 배포 확인

- 먼저 Pod가 정상적으로 띄어졌는지 확인하기 위해 다음의 명령어를 사용한다.

```bash
kubectl get pod
```

![image](https://user-images.githubusercontent.com/32426312/131827899-1c3f81c3-f3bb-4e5e-9beb-e68bfee79c14.png)

- Pod는 모두 정상적으로 동작함을 확인했다.
- 이제 관련된 Service도 정상적으로 동작하는지 확인한다.

```bash
kubectl get svc
```

![image](https://user-images.githubusercontent.com/32426312/131827936-8df5094e-ae9d-496f-8e4a-923339af2a40.png)

- gateway는 LoadBalancer 타입으로, 나머지는 ClusterIP 타입으로 정상 배포가 된 것을 확인할 수 있다.


#### 배포된 상태로 API TEST

- 이제 클라우드에 배포가 되었으니, 로컬 형태가 아닌 External IP 형태로 REST API TEST를 진행한다.
- 위 명령어에서 Gateway의 External IP를 얻었으니 그것으로 접근을 시도한다.


#### 통신 테스트

```bash
http GET EXTERNALIP:8080/orders
```

![image](https://user-images.githubusercontent.com/32426312/131831839-9115da37-20e4-450b-b784-279a69602e56.png)



#### Gateway 에서 주문처리 (차량 신청 처리) 

```java
http EXTERNALIP:8080/orders carNumber=132누8781 carBrand=쏘나타 carPost=판교역3번출구 userName=Lee status=차량신청_Polyglot
http EXTERNALIP:8080/orders carNumber=101가1231 carBrand=아반떼 carPost=우림빌딩 userName=Park status=차량신청_Polyglot
```

- 실행결과  

![image](https://user-images.githubusercontent.com/32426312/131833139-64a7a9c2-9f8e-4b63-979d-548a245d941f.png)

![image](https://user-images.githubusercontent.com/32426312/131833174-320197c6-09ac-4f6f-9003-468a2da2e620.png)
  
&nbsp;

#### Gateway에서 Payment 조회

```java
http EXTERNALIP:8080/paymentHistories
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131833361-e5626242-4255-4367-8840-d500872c9438.png)

	
&nbsp;

#### Gateway에서 Reservation 조회


```java
http EXTERNALIP:8080/reservations
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131833448-b915a249-0696-42b4-89a5-45fbc5e88474.png)

&nbsp;


#### Gateway에서 MyPage 조회


```java
http EXTERNALIP:8080/myPages
```

- 실행결과

![image](https://user-images.githubusercontent.com/32426312/131833622-bd7fbad8-2da7-4cfa-8a81-56237f48c0ab.png)

&nbsp;




## Circuit Breaker (Check-Point)

- Circuit Breaker를 구현하기 위해선, 비동기식 호출이 아닌 동기식 호출 관계여야 한다.
- 동기식 호출에 사용되는 Spring의 Feign Client와 Yaml 파일에 Hystrix 옵션을 추가하면 Circuit Breaker를 구현할 수 있다.

- 현재 프로젝트에서는 Order-->Payment로 가는 방향에서 Sync (Res/Resp) 호출이 구현되어 있다.
- 이에 따라 주문이 들어오고 결제 요청이 과도할 경우, Circuit Breaker를 통해 일시적으로 장애를 격리시키는 것을 구현한다.


### Application.yml 파일 수정

- Hystrix 를 설정:  요청의 처리시간이 610 밀리초가 넘어서고 그 현상이 어느정도 유지되면 Circuit Breaker 동작.

```bash

feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

### Order.java 수정 

- 호출을 받는 쪽에서 부하가 되도록 임의로 시간 지연을 설정

```java

   @PostPersist
    public void onPostPersist(){


        OrderPlaced orderPlaced = new OrderPlaced();
        orderPlaced.setStatus("Car is Selected, This order id is :" + this.id);
        System.out.println("Car is Selected, This order id is :" + this.id);
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();
        
        try 
        { 
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

```

### Siege.yml 생성 및 배포

- 부하테스트에 필요한 툴인 siege를 사용하기 위해 yml 파일로 배포한다.

```bash
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
    - name: siege
      image: apexacme/siege-nginx
```

- 위와 같이 yml 파일을 작성하여 저장한 뒤, 다음의 명령어로 배포한다.

```bash
kubectl apply -f siege.yml
```


### 부하테스트

- siege 툴을 사용하여, Circuit Breaker가 동작하는지 확인한다.
- 동시사용자 45명, 55초 동안 실시
- 다음의 명령어를 사용한다.

```bash
kubectl exec -it siege -- bash
siege -v -c45 -t55S --content-type "application/json" 'http://sharecar-order:8080/orders POST {"carBrand":"쏘나타","carNumber":"01누1111","status":"차량신청"}'
```

![image](https://user-images.githubusercontent.com/32426312/131863075-001d0164-9ec9-4258-a09e-49e14a2fa0cb.png)


![image](https://user-images.githubusercontent.com/32426312/131863204-8281e459-c56d-417b-a0e4-e48ab903b838.png)


- 위 사진에서, 중간에 Request가 빨간불이 되었다가 파란불이 되는 것을 반복하는것이 보인다.
- 부하가 걸려 Circuit Breaker가 작동했음을 알 수 있다.




## Autoscale HPA (Check-Point)

### deployment.yml 파일 수정

- 먼저 테스트 타겟이 될 deployment를 수정해야 한다. 
- 다음과 같은 명령어를 실행하여 deployment를 연다.

```bash
kubectl edit deployment sharecar-reservation
```

- 그 후, Resoure 부분을 찾아 request와 limits 항목을 추가해준다.

![image](https://user-images.githubusercontent.com/32426312/131873765-897ec81d-f2c4-49bf-9774-c212dc3133cb.png)



### Autoscale 설정

- Pod가 Autoscale 되도록 다음의 명령어를 실행하여 설정한다.

```bash
kubectl autoscale deployment sharecar-order --cpu-percent=50 --min=1 --max=10
```

![image](https://user-images.githubusercontent.com/32426312/131874087-1e18a912-5845-4661-9eb0-947714f9dd32.png)

- 잘 적용 되었는지 다음의 명령어로 확인 가능하다.

```bash
kubectl get horizontalpodautoscaler
kubectl get hpa
```

![image](https://user-images.githubusercontent.com/32426312/131874166-28ac64b8-7218-49b3-b3e9-695e3f1c2192.png)


### 부하테스트

- 부하를 주기 전, 실시간으로 pod의 scaling을 모니터링 할 터미널을 띄운다.

```bash
watch kubectl get pod
```

![image](https://user-images.githubusercontent.com/32426312/131871551-81fa0700-70ef-4214-b4eb-45c8c9f0eb0a.png)


- 이제 타겟 Deployment로 부하를 준다.

```bash
kubectl exec -it siege -- bash
siege -v -c30 -t30s http://sharecar-reservation:8080
```

![image](https://user-images.githubusercontent.com/32426312/131874511-89dd8852-d70d-4110-8ee2-75f1aec3127d.png)


- 첫번째 변화
![image](https://user-images.githubusercontent.com/32426312/131874339-ea20569e-b5f2-4081-9d33-654aa99c20ad.png)

- 두번째 변화
![image](https://user-images.githubusercontent.com/32426312/131874368-72091c31-9428-4ecd-8a48-528b17e7d567.png)

- 세번째 변화
![image](https://user-images.githubusercontent.com/32426312/131874634-d83d4652-5517-461c-9436-78dc69fd4871.png)



## Readiness Probe : Zero-downtime deploy (Check-Point)

- Readiness Probe가 잘 동작하는지 확인하려면, 무정지 배포가 잘 되는지 확인하면 된다.
- 무정지 배포란, 코드를 수정하고 새롭게 배포했을때 쿠버네티스가 성급히 기존 Pod를 지우지 않고 새로운 Pod가 안전히 동작할 때까지 기다리는 것이다.
- 그 후 새로운 Pod가 안전히 동작하면 기존 Pod를 지운다.


### Readiness 설정

- Readiness Probe를 설정하기 위해서는, 쿠버네티스에 배포하는 Deployment.yml 파일에 내용을 추가해주어야 한다.
- 현재는 Codebuild를 통한 CI/CD 설정으로 buildspec.yml 파일에 deployment 내용이 있으므로 그곳에 추가해준다.

![image](https://user-images.githubusercontent.com/32426312/131879044-10d90609-8cac-49d0-bdd0-d61e59bc5465.png)

- 그리고 배포 후에 deployment 파일을 열어 잘 적용되어 있는지 확인해준다.

```bash
kubectl edit deployment sharecar-mypage
```

![image](https://user-images.githubusercontent.com/32426312/131879261-f1e92607-c3a7-4a98-97ff-8156d37bf8a7.png)


### TEST 배포

- 이제 Readiness Probe를 확인하기 위해, 타겟인 sharecar-mypage의 github에서 코드를 수정한다.
- CI/CD 설정으로 인해 AWS Codebuild가 자동으로 빌드 및 배포를 진행한다.
- 그 과정에서 무중단배포가 잘 이루어지는지 확인한다.

![image](https://user-images.githubusercontent.com/32426312/131879741-1ede5edb-e996-4c9d-a07a-e96fbbd7567e.png)



### 터미널에서 Zero-downtime deploy 확인

- 초기 상태 : 기존 Pod만 살아있음

![image](https://user-images.githubusercontent.com/32426312/131880405-72472f18-9cf8-4532-bf75-ec12643c26d7.png)

- 첫번째 변화 : 새로운 Pod가 올라왔지만 아직 정상동작을 하지 않기에 기존 Pod와 공존함

![image](https://user-images.githubusercontent.com/32426312/131880472-1bc025fa-85ad-43bc-bdda-a83924dfcd0b.png)

- 두번째 변화 : 새로운 Pod가 정상적으로 동작하지만 완벽한 무중단을 위해 기존 Pod를 바로 지우지 않음.

![image](https://user-images.githubusercontent.com/32426312/131880640-d48f59dd-54e8-42e6-9ed3-f32983f64145.png)

- 세번째 변화 : 새롭게 올라온 Pod가 문제가 없자 기존의 Pod를 지우고 대체가 완료됨.

![image](https://user-images.githubusercontent.com/32426312/131880743-c968a4fc-f90d-4593-a1b8-31eb90f524d1.png)


## Liveness Probe : Self-healing (Check-Point)

- Liveness Probe가 잘 동작하는지 확인하려면, 잘 동작하고 있는 Pod를 강제로 지워보면 된다.
- Liveness Probe란, Pod 상태를 계속 체크하고 있다가 비정상이 감지될 경우 Pod를 재시작한다.
- 만약 Liveness Probe가 잘 동작한다면, Pod를 강제로 지웠을때 비정상을 감지하고 Pod를 재시작 할 것이다.

### Livenss 설정

- Readiness Probe를 설정하기 위해서는, 쿠버네티스에 배포하는 Deployment.yml 파일에 내용을 추가해주어야 한다.
- 현재는 Codebuild를 통한 CI/CD 설정으로 buildspec.yml 파일에 deployment 내용이 있으므로 그곳에 추가해준다.

![image](https://user-images.githubusercontent.com/32426312/131881448-38e72922-b1af-4fe7-a682-2cf7d2fd1093.png)

- 그리고 배포 후에 deployment 파일을 열어 잘 적용되어 있는지 확인해준다.

```bash
kubectl edit deployment sharecar-mypage
```

![image](https://user-images.githubusercontent.com/32426312/131881534-35305d1c-0405-47fd-aa02-3e3285c89bcc.png)


### 터미널에서 Self-healing 확인

- 터미널에서 타겟 Pod를 강제로 지운다.

```bash
kubectl get pod
kubectl delete pod [pod이름]
```

![image](https://user-images.githubusercontent.com/32426312/131882079-7f2394a8-52c4-4843-bcc3-d7ea7735faab.png)

- 그 후 pod를 조회하여 어떤 현상이 발생하는지 본다.

![image](https://user-images.githubusercontent.com/32426312/131882146-08706093-b21b-4bc0-8532-75bb66f8bb32.png)

- 위와 같이 Pod를 삭제하자 마자 새로운 Pod를 띄우고 활성화 시키려고 한다.

![image](https://user-images.githubusercontent.com/32426312/131882307-0afb3510-4d1b-4dcc-b93f-f4575ac5ba53.png)

- 그리고 얼마 지나지 않고 새롭게 뜬 Pod는 정상적으로 동작한다.



## Configmap (Check-Point)

- configmap을 생성하는 방법에는 단순 CLI로 하는 법과 Yaml 파일을 작성해서 배포하는 방법이 있다.
- CLI로 생성하는 방법은 다음과 같다.

```bash
kubectl create configmap test-config --from-literal=language=java --from-literal=level=expert
```

![image](https://user-images.githubusercontent.com/32426312/131892104-0733e8fd-f325-445d-987f-c69a46d5805b.png)


- 잘 생성되었는지 정보를 가져온다.

```bash
kubectl get cm
kubectl get configmaps [이름] -o yaml
```

![image](https://user-images.githubusercontent.com/32426312/131892407-2c69727b-3859-4164-8de7-c30e1125a433.png)


- 다음은 yaml 파일로 만들어서 생성하는 방법이다.
- testconfig2.yml을 생성하여 다음과 같이 작성한다.

```bash
apiVersion: v1
kind: ConfigMap
metadata:
  name: configtest
data:
  TEXT1: This
  TEXT2: is
  TEXT3: Configmaptest
```

- 그후 배포까지 진행한다.

```bash
kubectl apply -f testconfig2.yml 
```

![image](https://user-images.githubusercontent.com/32426312/131895111-7d91c438-ceb8-45d6-aa22-6ea206c9a582.png)

![image](https://user-images.githubusercontent.com/32426312/131895361-0cb9c590-7bae-49b9-9033-0c318f0516f7.png)




## Secret

- Configmap과 함께 쓰이는 Secret을 생성한다

```
kubectl create secret generic my-password --from-literal=password=testconfigpassword
```

![image](https://user-images.githubusercontent.com/32426312/131892784-1cb2b186-cd41-428f-9659-57304672f463.png)

- 잘 생성되었는지 확인해본다.

```bash
kubectl get secret my-password -o yaml
```

![image](https://user-images.githubusercontent.com/32426312/131892968-1afe8ef3-1304-4a1c-aebd-ccfa0888fd88.png)

- 위에서 볼 수 있듯이 현재 비밀번호는 암호화 되어서 들어갔는데, 잘 암화화 되었는지 다음과 같이 확인이 가능하다.

![image](https://user-images.githubusercontent.com/32426312/131894125-faf2fd76-076b-4043-a359-e492c4098d41.png)


