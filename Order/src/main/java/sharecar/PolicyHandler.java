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
	private OrderRepository orderRepository;
	
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserveAccepted_(@Payload ReservationAccepted reserveAccepted){

        if(reserveAccepted.validate()){
            System.out.println("##### listener  : " + reserveAccepted.toJson());
//            Optional<Order> orderOptional = orderRepository.findById(reserveAccepted.getOrderId());
//            Order order = orderOptional.get();
//            order.setStatus(shipped.getStatus());
            
//            Order order = new Order();
//            order.setId(reserveAccepted.getOrderId());
//            order.setStatus("Final Complete");
//          orderRepository.save(order);
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserveCanceled_(@Payload ReservationCanceled reserveCanceled){

        if(reserveCanceled.validate()){
//            System.out.println("##### listener  : " + reserveCanceled.toJson());
//        	Order order = new Order();
//        	order.setId(reserveCanceled.getId());
//        	order.setStatus("Reserve Cancelled");
//        	orderRepository.save(order);
        }
    }

}
