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
