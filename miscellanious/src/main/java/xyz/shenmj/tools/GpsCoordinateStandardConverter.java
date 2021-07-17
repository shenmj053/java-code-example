package xyz.shenmj.tools;

/**
 * GPS 坐标系转换
 * <p>
 * WGS84坐标系：即地球坐标系，国际上通用的坐标系。设备一般包含GPS芯片或者北斗芯片获取的经纬度为WGS84地理坐标系, 谷歌地图使用WGS84（中国范围除外）。
 * GCJ02坐标系：即火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统，由WGS84坐标系经加密后的坐标系。高德地图使用GCJ02。
 * BD09坐标系：即百度坐标系，GCJ02坐标系经加密后的坐标系。百度地图使用BD09。
 *
 * @author SHEN Minjiang
 */
public class GpsCoordinateStandardConverter {
    private static final double xPi = 3.14159265358979324 * 3000.0 / 180.0;
    // π
    private static final double pi = 3.1415926535897932384626;
    // 长半轴
    private static final double a = 6378245.0;
    // 扁率
    private static final double ee = 0.00669342162296594323;

    /**
     * 百度坐标系BD09转地球坐标系WGS84
     *
     * @param longitude 百度坐标纬度
     * @param latitude  百度坐标经度
     * @return WGS84坐标数组
     */
    public static double[] BD09ToWGS84(double longitude, double latitude) {
        double[] gcj = BD09ToGCJ02(longitude, latitude);
        return GCJ02ToWGS84(gcj[0], gcj[1]);
    }

    /**
     * 地球坐标系WGS84坐标转百度坐标系BD09
     *
     * @param longitude WGS84坐标系的经度
     * @param latitude  WGS84坐标系的纬度
     * @return 百度坐标数组BD09
     */
    public static double[] WGS84ToBD09(double longitude, double latitude) {
        double[] gcj = WGS84ToGCJ02(longitude, latitude);
        return GCJ02ToBD09(gcj[0], gcj[1]);
    }

    /**
     * 火星坐标系GCJ02转百度坐标系BD09
     *
     * @param longitude 火星坐标经度
     * @param latitude  火星坐标纬度
     * @return 百度坐标数组
     */
    public static double[] GCJ02ToBD09(double longitude, double latitude) {
        double z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * xPi);
        double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * xPi);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[]{bd_lng, bd_lat};
    }

    /**
     * 百度坐标系BD09转火星坐标系GCJ02
     *
     * @param longitude 百度坐标纬度
     * @param latitude  百度坐标经度
     * @return 火星坐标数组
     */
    public static double[] BD09ToGCJ02(double longitude, double latitude) {
        double x = longitude - 0.0065;
        double y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * xPi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * xPi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lng, gg_lat};
    }

    /**
     * 地球坐标系WGS84转火星坐标系GCJ02
     *
     * @param longitude WGS84坐标系的经度
     * @param latitude  WGS84坐标系的纬度
     * @return 火星坐标数组
     */
    public static double[] WGS84ToGCJ02(double longitude, double latitude) {
        if (isOutOfChina(longitude, latitude)) {
            return new double[]{longitude, latitude};
        }
        double dlat = transformLongitude(longitude - 105.0, latitude - 35.0);
        double dlng = transformLatitude(longitude - 105.0, latitude - 35.0);
        double radlat = latitude / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = latitude + dlat;
        double mglng = longitude + dlng;
        return new double[]{mglng, mglat};
    }

    /**
     * 火星坐标系GCJ02转地球坐标系WGS84
     *
     * @param longitude 火星坐标系的经度
     * @param latitude  火星坐标系纬度
     * @return WGS84坐标数组
     */
    public static double[] GCJ02ToWGS84(double longitude, double latitude) {
        if (isOutOfChina(longitude, latitude)) {
            return new double[]{longitude, latitude};
        }
        double dlat = transformLongitude(longitude - 105.0, latitude - 35.0);
        double dlng = transformLatitude(longitude - 105.0, latitude - 35.0);
        double radlat = latitude / 180.0 * pi;
        double magic = Math.sin(radlat);
        magic = 1 - ee * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * pi);
        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * pi);
        double mglat = latitude + dlat;
        double mglng = longitude + dlng;
        return new double[]{longitude * 2 - mglng, latitude * 2 - mglat};
    }

    /**
     * 纬度转换
     */
    public static double transformLongitude(double longitude, double latitude) {
        double ret = -100.0 + 2.0 * longitude + 3.0 * latitude + 0.2 * latitude * latitude + 0.1 * longitude * latitude + 0.2 * Math.sqrt(Math.abs(longitude));
        ret += (20.0 * Math.sin(6.0 * longitude * pi) + 20.0 * Math.sin(2.0 * longitude * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(latitude * pi) + 40.0 * Math.sin(latitude / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(latitude / 12.0 * pi) + 320 * Math.sin(latitude * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度转换
     */
    public static double transformLatitude(double longitude, double latitude) {
        double ret = 300.0 + longitude + 2.0 * latitude + 0.1 * longitude * longitude + 0.1 * longitude * latitude + 0.1 * Math.sqrt(Math.abs(longitude));
        ret += (20.0 * Math.sin(6.0 * longitude * pi) + 20.0 * Math.sin(2.0 * longitude * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(longitude * pi) + 40.0 * Math.sin(longitude / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(longitude / 12.0 * pi) + 300.0 * Math.sin(longitude / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 是否在国外
     *
     * @param longitude 经度
     * @param latitude  维度
     * @return 是否在国外
     */
    public static boolean isOutOfChina(double longitude, double latitude) {
        if (longitude < 72.004 || longitude > 137.8347) {
            return true;
        } else if (latitude < 0.8293 || latitude > 55.8271) {
            return true;
        }
        return false;
    }

    /**
     * 是否在国内
     *
     * @param longitude 经度
     * @param latitude  维度
     * @return 是否在国内
     */
    public static boolean isInsideChina(double longitude, double latitude) {
        return !isOutOfChina(longitude, latitude);
    }
}