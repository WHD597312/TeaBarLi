package teabar.ph.com.teabar.pojo;

import java.io.Serializable;

public class Tea implements Serializable {
        long  id ;
        long teaId;
    String teaNameCn ;//名稱
        String teaNameEn;
        String typeCn;//类型
        String  typeEn;
        String ingredientCn;//成分
      String ingredientEn;
       String synopsisCn;//功能
       String synopsisEn;
       String teaPicture;
        String productNameEn;
     String productNameCn;
    String tasteCn;
    String tasteEn;
    String teaPhoto;
    String accountCn;
    String accountEn;
    String rgb;
    String shopId;
    int temperature;//温度
    int  seconds;//浸泡时间
    int waterYield;//体积
    boolean love ;
    int isCollection;

    public String getAccountCn() {
        return accountCn;
    }

    public void setAccountCn(String accountCn) {
        this.accountCn = accountCn;
    }

    public String getAccountEn() {
        return accountEn;
    }

    public void setAccountEn(String accountEn) {
        this.accountEn = accountEn;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeaId() {
        return teaId;
    }

    public void setTeaId(long teaId) {
        this.teaId = teaId;
    }

    public String getTeaNameCn() {
        return teaNameCn;
    }

    public void setTeaNameCn(String teaNameCn) {
        this.teaNameCn = teaNameCn;
    }

    public String getTeaNameEn() {
        return teaNameEn;
    }

    public void setTeaNameEn(String teaNameEn) {
        this.teaNameEn = teaNameEn;
    }

    public String getTypeCn() {
        return typeCn;
    }

    public void setTypeCn(String typeCn) {
        this.typeCn = typeCn;
    }

    public String getTypeEn() {
        return typeEn;
    }

    public void setTypeEn(String typeEn) {
        this.typeEn = typeEn;
    }

    public String getIngredientCn() {
        return ingredientCn;
    }

    public void setIngredientCn(String ingredientCn) {
        this.ingredientCn = ingredientCn;
    }

    public String getIngredientEn() {
        return ingredientEn;
    }

    public void setIngredientEn(String ingredientEn) {
        this.ingredientEn = ingredientEn;
    }

    public String getSynopsisCn() {
        return synopsisCn;
    }

    public void setSynopsisCn(String synopsisCn) {
        this.synopsisCn = synopsisCn;
    }

    public String getSynopsisEn() {
        return synopsisEn;
    }

    public void setSynopsisEn(String synopsisEn) {
        this.synopsisEn = synopsisEn;
    }

    public String getTeaPicture() {
        return teaPicture;
    }

    public void setTeaPicture(String teaPicture) {
        this.teaPicture = teaPicture;
    }

    public String getProductNameEn() {
        return productNameEn;
    }

    public void setProductNameEn(String productNameEn) {
        this.productNameEn = productNameEn;
    }

    public String getProductNameCn() {
        return productNameCn;
    }

    public void setProductNameCn(String productNameCn) {
        this.productNameCn = productNameCn;
    }

    public String getTasteCn() {
        return tasteCn;
    }

    public void setTasteCn(String tasteCn) {
        this.tasteCn = tasteCn;
    }

    public String getTasteEn() {
        return tasteEn;
    }

    public void setTasteEn(String tasteEn) {
        this.tasteEn = tasteEn;
    }

    public String getTeaPhoto() {
        return teaPhoto;
    }

    public void setTeaPhoto(String teaPhoto) {
        this.teaPhoto = teaPhoto;
    }

    public String getRgb() {
        return rgb;
    }

    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getWaterYield() {
        return waterYield;
    }

    public void setWaterYield(int waterYield) {
        this.waterYield = waterYield;
    }

    public boolean isLove() {
        return love;
    }

    public void setLove(boolean love) {
        this.love = love;
    }

    public int getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(int isCollection) {
        this.isCollection = isCollection;
    }
}
