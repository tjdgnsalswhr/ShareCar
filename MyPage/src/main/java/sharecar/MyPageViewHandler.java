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

