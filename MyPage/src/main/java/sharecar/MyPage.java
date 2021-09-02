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
        /**/
        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

}
