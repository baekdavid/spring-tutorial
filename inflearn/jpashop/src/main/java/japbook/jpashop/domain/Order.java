package japbook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

  @Id
  @GeneratedValue
  @Column(name = "order_id")
  private Long id;

  // Order 랑 Membre 는 다대일 관계이기 때문에 아래와 같은
  //   연관 관계 에노테이션 사용
  @ManyToOne(fetch = LAZY) // 연관 관계의 주인은 이와 같이 옵션이 없다.
  @JoinColumn(name = "member_id") // foreign key = member_id 를 의미
  private Member member;

  // cascade : 데이터 변경에 대해서 전파한다.
  @OneToMany(mappedBy = "order", cascade = ALL)
  private List<OrderItem> orderItems = new ArrayList<>();

  // Order 쪽에서 DB 접근이 많으므로 Order가 연관 관계의 주인이 된다.
  @OneToOne(fetch = LAZY, cascade = ALL) // 일대일 관계
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  private LocalDateTime orderDate; // 주문시간

  @Enumerated(EnumType.STRING)
  private OrderStatus status; // 주문상태 [ORDER, CANCEL]

  // == 연관관계 메서드 (양방향 관계일 때 만든다) == //
  public void setMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void setDelivery(Delivery delivery) {
    this.delivery = delivery;
    delivery.setOrder(this);
  }

  // == 생성 메서드 == //
  public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
    Order order = new Order();
    order.setMember(member);
    order.setDelivery(delivery);
    Arrays.stream(orderItems).forEach(order::addOrderItem);
    order.setStatus(OrderStatus.ORDER);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }

  // == 비즈니스 로직 == //
  /**
   * 주문 취소
   */
  public void cancel() {
    if (delivery.getStatus() == DeliveryStatus.COMP) {
      throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
    }
    this.setStatus(OrderStatus.CANCEL);
    orderItems.forEach(OrderItem::cancel);
  }

  // == 조회 로직 == //

  /**
   * 전체 주문 가격 조회
   */
  public int getTotalPrice() {
    return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
  }

}