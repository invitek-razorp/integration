package com.invitekaro.model;


public class Card {
    private String cardType,cardName,cardDetail;

    public Card(){}
    public Card(String cardType, String cardName, String cardDetail) {
        cardType = cardType;
        cardName = cardName;
        cardDetail = cardDetail;
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

    public String getCardDetail() {
        return cardDetail;
    }
    public void  setCardDetail(String cardDetail) {
        this.cardDetail = cardDetail;
    }
}
