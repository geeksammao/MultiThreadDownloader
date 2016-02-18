package geeksammao.bingyan.net.mydownloader.network;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import geeksammao.bingyan.net.mydownloader.network.result.RequestResult;
import geeksammao.bingyan.net.mydownloader.util.Logger;

/**
 * Created by Geeksammao on 10/8/15.
 */
public class HttpUtil {
    public static final int HTTP_OK = 200;
    public static final int HTTP_ERROR = 404;
    public static final int HTTP_CLIENT_TIMEOUT = 403;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_PARTIAL = 206;

    private long startPosition;
    private long endPosition;
    private boolean isRangeEnabled;

    private volatile static HttpUtil httpUtil;

    public static HttpUtil getInstance() {
        if (httpUtil == null) {
            synchronized (HttpUtil.class) {
                if (httpUtil == null) {
                    httpUtil = new HttpUtil();
                }
            }
        }
        return httpUtil;
    }

    private HttpUtil() {
    }

    public void setStartPosition(long startPosition) {
        isRangeEnabled = true;
        this.startPosition = startPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public RequestResult<String> get(String targetUrl) {
        HttpURLConnection urlConnection = null;
        RequestResult<String> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
            setUrlConnectionWithGetMethod(urlConnection);
            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpsURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(inputStreamToString(urlConnection.getInputStream()));
                    break;
                case HttpsURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_CLIENT_TIMEOUT);
                    requestResult.setData(null);
                    break;
                case HttpsURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_GATEWAY_TIMEOUT);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            Logger.logString(this, "404 error");
            requestResult.setStatus(HTTP_ERROR);
            requestResult.setData(null);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return requestResult;
    }

    public RequestResult<Integer> getContentLength(String targetUrl) {
        HttpURLConnection urlConnection = null;
        RequestResult<Integer> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            setUrlConnectionWithHeadMethod(urlConnection);
            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(urlConnection.getContentLength());
                    break;
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_ERROR);
                    requestResult.setData(null);
                    break;
                case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_ERROR);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            requestResult.setStatus(HTTP_ERROR);
            requestResult.setData(null);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return requestResult;
    }

    public final RequestResult<InputStream> getInputStream(String targetUrl) {
        HttpURLConnection urlConnection;
        RequestResult<InputStream> requestResult = new RequestResult<>();

        try {
            URL url = new URL(targetUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            setUrlConnectionWithGetMethod(urlConnection);
            if (isRangeEnabled) {
                urlConnection.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);
            }

            urlConnection.connect();

            switch (urlConnection.getResponseCode()) {
                case HttpsURLConnection.HTTP_OK:
                    requestResult.setStatus(HTTP_OK);
                    requestResult.setData(urlConnection.getInputStream());
                    break;
                case HttpsURLConnection.HTTP_PARTIAL:
                    requestResult.setStatus(HTTP_PARTIAL);
                    requestResult.setData(urlConnection.getInputStream());
                    break;
                case HttpsURLConnection.HTTP_CLIENT_TIMEOUT:
                    requestResult.setStatus(HTTP_CLIENT_TIMEOUT);
                    requestResult.setData(null);
                    break;
                case HttpsURLConnection.HTTP_GATEWAY_TIMEOUT:
                    requestResult.setStatus(HTTP_GATEWAY_TIMEOUT);
                    requestResult.setData(null);
                    break;
                default:
                    requestResult.setStatus(urlConnection.getResponseCode());
                    requestResult.setData(null);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            requestResult.setStatus(urlConnection.getResponseCode());
            requestResult.setData(null);
        } finally {
            // reset the range property
            // to handle multi-thread call
            resetTheRangeProperty();
        }

        return requestResult;
    }

    private void resetTheRangeProperty() {
        endPosition = 0;
        startPosition = 0;
    }

    private void setUrlConnectionWithGetMethod(HttpURLConnection urlConnection) throws ProtocolException {
        urlConnection.setConnectTimeout(5 * 1000);
        urlConnection.setReadTimeout(5 * 1000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "image/gif,image/jpeg," +
                "image/pjpeg,application/x-shockwave-flash,application/xaml+xml," +
                "application/vnd.ms-xpsdocument,application/x-ms-xbap," +
                "application/x-ms-application,application/vnd.ms-excel," +
                "application/vnd.ms-powerpoint,application/msword,*/*");
        urlConnection.setRequestProperty("Charset", "UTF-8");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0(" +
                "compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; " +
                ".NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; " +
                ".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        urlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
        urlConnection.setRequestProperty("Connection", "Keep-alive");
        urlConnection.setDefaultUseCaches(true);
    }

    private void setUrlConnectionWithHeadMethod(HttpURLConnection urlConnection) throws ProtocolException {
        urlConnection.setConnectTimeout(5 * 1000);
        urlConnection.setReadTimeout(5 * 1000);
        urlConnection.setRequestMethod("HEAD");
        urlConnection.setRequestProperty("Accept", "image/gif,image/jpeg," +
                "image/pjpeg,application/x-shockwave-flash,application/xaml+xml," +
                "application/vnd.ms-xpsdocument,application/x-ms-xbap," +
                "application/x-ms-application,application/vnd.ms-excel," +
                "application/vnd.ms-powerpoint,application/msword,*/*");
        urlConnection.setRequestProperty("Charset", "UTF-8");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0(" +
                "compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; " +
                ".NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; " +
                ".NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
        urlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
        urlConnection.setRequestProperty("Connection", "Keep-alive");
        urlConnection.setDefaultUseCaches(true);
    }

    public RequestResult<InputStream> getInputStreamWithOkhttp(String targetUrl) {
        RequestResult<InputStream> requestResult = new RequestResult<>();

        final OkHttpClient client = new OkHttpClient();
        int maxStale = 60 * 60 * 24 * 28;
        Request request = new Request.Builder().url(targetUrl).
                header("Range", "bytes=" + startPosition + "-" + endPosition).
                addHeader("Accept-Ranges", "bytes").
                build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                requestResult.setStatus(HTTP_PARTIAL);
                requestResult.setData(response.body().byteStream());
            } else {
                Logger.logString(this, "response is error");
                requestResult.setStatus(response.code());
                requestResult.setData(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return requestResult;
    }

    public String getFileName(String url) {
        String filename = "";
        URL myURL;
        HttpURLConnection conn = null;
        if (url == null || url.length() < 1) {
            return null;
        }

        try {
            myURL = new URL(url);
            conn = (HttpURLConnection) myURL.openConnection();
            setUrlConnectionWithHeadMethod(conn);
            conn.connect();

            if (conn.getResponseCode() != HTTP_OK) {
                return null;
            } else {
                URL absUrl = conn.getURL();
                filename = conn.getHeaderField("Content-Disposition");
                if (filename == null || filename.length() < 1) {
                    filename = absUrl.getFile();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        if (filename.contains("/")){
            filename = filename.substring(filename.lastIndexOf("/") + 1,filename.length());
        }
        return filename;
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }
}

