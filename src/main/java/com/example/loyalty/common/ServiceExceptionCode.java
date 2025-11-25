package com.example.loyalty.common;

public enum ServiceExceptionCode {
    INTERNAL_ERROR("내부 오류"),
    VALIDATION_ERROR("잘못된 요청"),
    NOT_FOUND_USER("사용자를 찾을 수 없습니다"),
    NOT_FOUND_PRODUCT("상품을 찾을 수 없습니다"),
    NOT_FOUND_ORDER("주문을 찾을 수 없습니다"),
    NOT_FOUND_WALLET("포인트 지갑을 찾을 수 없습니다"),
    NOT_FOUND_REFUND("환불 정보를 찾을 수 없습니다"),
    CART_LIMIT_EXCEEDED("장바구니 최대 허용 수량을 초과했습니다"),
    CART_EMPTY("장바구니가 비어 있습니다"),
    POINT_BALANCE_SHORTAGE("포인트 잔액이 부족합니다"),
    POINT_MINIMUM_VIOLATION("포인트 사용 최소 조건을 만족하지 않습니다"),
    ORDER_ALREADY_COMPLETED("이미 완료된 주문입니다"),
    ORDER_REFUND_NOT_ALLOWED("환불을 진행할 수 없는 상태입니다"),
    PRODUCT_STOCK_SHORTAGE("상품 재고가 부족합니다"),
    PAYMENT_FAILED("결제에 실패했습니다"),
    FRAUD_SUSPECTED("사기 거래 의심입니다");

    private final String message;

    ServiceExceptionCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
