package teabar.ph.com.teabar.pojo;

public class Weather {
    String week;
    String wea;//天气
    String air_level;//风速
    String humidity;
    String tem;

    public Weather() {
    }

    public Weather(String week, String wea, String humidity, String tem) {
        this.week = week;
        this.wea = wea;

        this.humidity = humidity;
        this.tem = tem;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWea() {
        return wea;
    }

    public void setWea(String wea) {
        this.wea = wea;
    }

    public String getAir_level() {
        return air_level;
    }

    public void setAir_level(String air_level) {
        this.air_level = air_level;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTem() {
        return tem;
    }

    public void setTem(String tem) {
        this.tem = tem;
    }
}
