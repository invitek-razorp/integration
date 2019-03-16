package com.invitekaro.model;


public class Card {
    private String cardType;
    private String cardName;
    private String cardDetail;
    private String cardPrice;

    public Card(){}
    public Card(String cardType, String cardName, String cardDetail, String cardPrice) {
        cardType = cardType;
        cardName = cardName;
        cardDetail = cardDetail;
        cardPrice = cardPrice;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public void  setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void  setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void  setCardPrice(String cardPrice) {
        this.cardPrice = cardPrice;
    }

    public void  setCardDetail(String cardDetail) {
        this.cardDetail = cardDetail;
    }

    public String getCardDetail() {
        return cardDetail;
    }

    public double getCardPriceValue(){
        return Double.parseDouble(this.cardPrice);
    }

    public String getCardPrice(){
        return this.cardPrice;
    }

}
