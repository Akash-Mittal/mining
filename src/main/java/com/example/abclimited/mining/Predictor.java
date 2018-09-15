package com.example.abclimited.mining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Predictor extends PredictorTemplate {
    protected static Logger log = Logger.getLogger(Predictor.class.getName());

    @Override
    void predict(PLANS plan, PredictionStrategy predictionStrategy) {
        super.predict(plan, predictionStrategy);
    }

    public static void main(String[] args) {
        PLANS plan = PLANS.valueOf(System.getProperty("pred.plan", PLANS.MIDTERM.name()));
        PredictorTemplate predictorTemplate = new Predictor();
        log.fine("Finding Next Date for Plan:" + plan.name() + " Using: "
                + PredictionStrategy.DAYS_AVERAGE_ELAPSED_STRATEGY.name());
        predictorTemplate.predict(plan, PredictionStrategy.DAYS_AVERAGE_ELAPSED_STRATEGY);
    }
}

abstract class PredictorTemplate {
    private static Logger logger = Logger.getLogger(PredictorTemplate.class.getName());

    void predict(PLANS plan, PredictionStrategy predictionStrategy) {
        Client<String, String> client = new HTTPClient();
        String response = client.get(plan.name());
        if (response != null) {
            Parser<String, Info> parser = new UserPlanParser();
            Info info = parser.parse(response);
            if (info != null && info.getReturns() != null && info.getReturns().size() > 0) {
                predictionStrategy.predict(info.getReturns());
            } else {
                logger.fine("Unable to Parse");
                // (Level.SEVERE, "Unable to Parse", null);
            }
        } else {
            logger.fine("Invalid Response");
            // (Level.SEVERE, "Invalid Response", null);
        }
    }

    enum PredictionStrategy {

        DAYS_AVERAGE_ELAPSED_STRATEGY {

            @Override
            public void predict(List<Return> returns) {
                final List<Long> diffList = new ArrayList<Long>();
                returns.forEach(returnss -> {
                    Date datee;
                    try {
                        datee = simpleDateFormat.parse(returnss.getTranDate());
                        calendar.setTime(currentDate);
                        calendar.setTime(datee);
                        Long timeDiffInMS = (currentDate.getTime() - datee.getTime()) / 1000 / 60 / 60 / 24;
                        diffList.add(timeDiffInMS);
                    } catch (java.text.ParseException e) {
                        log.log(Level.WARNING, "Unable to To parse Date", e);
                    }
                });

                long sum = diffList.stream().mapToLong(v -> v.longValue()).sum();
                while (sum > 11) {
                    sum = sum % 11;
                }
                calendar.setTime(currentDate);
                calendar.add(Calendar.DATE, Long.valueOf(sum).intValue());
                System.out.println("Next Date:" + simpleDateFormat.format(calendar.getTime()));
            }
        };

        public abstract void predict(List<Return> returns);

        private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        private static Date currentDate = new Date();
        private static Logger log = Logger.getLogger(UserPlanParser.class.getName());
        private static Calendar calendar = new GregorianCalendar();
    }

}

enum PLANS {
    LOWCAP, MIDCAP, HIGHCAP, SHORTTERM, MIDTERM, LONGTERM;
}

class Info {

    private String name;
    private String investmentType;
    private String investmentDate;
    private List<Return> returns = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Info withName(String name) {
        this.name = name;
        return this;
    }

    public String getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(String investmentType) {
        this.investmentType = investmentType;
    }

    public Info withInvestmentType(String investmentType) {
        this.investmentType = investmentType;
        return this;
    }

    public String getInvestmentDate() {
        return investmentDate;
    }

    public void setInvestmentDate(String investmentDate) {
        this.investmentDate = investmentDate;
    }

    public Info withInvestmentDate(String investmentDate) {
        this.investmentDate = investmentDate;
        return this;
    }

    public List<Return> getReturns() {
        return returns;
    }

    public void setReturns(List<Return> returns) {
        this.returns = returns;
    }

    public Info withReturns(List<Return> returns) {
        this.returns = returns;
        return this;
    }

}

class Return {

    private String tranDate;
    private String tranAmount;

    public String getTranDate() {
        return tranDate;
    }

    public void setTranDate(String tranDate) {
        this.tranDate = tranDate;
    }

    public Return withTranDate(String tranDate) {
        this.tranDate = tranDate;
        return this;
    }

    public String getTranAmount() {
        return tranAmount;
    }

    public void setTranAmount(String tranAmount) {
        this.tranAmount = tranAmount;
    }

    public Return withTranAmount(String tranAmount) {
        this.tranAmount = tranAmount;
        return this;
    }

}

// Test Cases needed for Parsing null/ invalid / name missing / dateformat etc
@SuppressWarnings("unchecked")
class UserPlanParser implements Parser<String, Info> {
    Logger log = Logger.getLogger(UserPlanParser.class.getName());

    @Override
    public Info parse(String jsonText) {
        Info info = new Info();
        List<Return> returns = new ArrayList<>();
        try {
            if (jsonText != null && !jsonText.isEmpty()) {
                JSONParser parser = new JSONParser();
                Object object = parser.parse(new StringReader(jsonText));
                JSONObject jsonObject = (JSONObject) object;
                info.withName((String) jsonObject.getOrDefault(Parser.JSONParams.name, Parser.JSONParams.invalid))
                        .withInvestmentType((String) jsonObject.getOrDefault(Parser.JSONParams.investmentType,
                                Parser.JSONParams.invalid))
                        .withInvestmentDate((String) jsonObject.getOrDefault(Parser.JSONParams.investmentDate,
                                Parser.JSONParams.invalid));

                JSONArray jsonArray = (JSONArray) jsonObject.get("returns");
                Iterator<JSONObject> iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    jsonObject = iterator.next();
                    Return returnn = new Return()
                            .withTranDate((String) jsonObject.getOrDefault(Parser.JSONParams.tranDate,
                                    Parser.JSONParams.invalid))
                            .withTranAmount((String) jsonObject.getOrDefault(Parser.JSONParams.tranAmount,
                                    Parser.JSONParams.invalid));
                    returns.add(returnn);
                }
                info.withReturns(returns);
            }
        } catch (IOException | ParseException e) {
            log.log(Level.SEVERE, "Unable to To parse Response", e);
        }

        return info;
    }

}

interface Parser<K, V> {

    interface JSONParams {
        String name = "name";
        String investmentType = "investmentType";
        String investmentDate = "investmentDate";
        String returns = "returns";
        String tranDate = "tranDate";
        String tranAmount = "tranAmount";
        String invalid = "invalid";

    }

    V parse(K k);

}

// Test Cases needed for Time out / Service Down / Invalid Response
class HTTPClient implements Client<String, String> {
    Logger log = Logger.getLogger(HTTPClient.class.getName());

    @Override
    public String get(String plan, String... optionalParams) {
        String output = null;
        try {

            URL url = new URL(Client.Params.URL + plan);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(Client.Params.GET);
            conn.setRequestProperty(Client.Params.HEADER_ACCEPT, Client.Params.APP_JSON);
            conn.setReadTimeout(Client.Params.READ_TIME_OUT);
            if (conn.getResponseCode() != Client.Params.HTTPCODES.TWO_HUNDERED.getVal()) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuffer stringBuffer = new StringBuffer();
            while ((output = br.readLine()) != null) {
                stringBuffer.append(output);
            }
            output = stringBuffer.toString();
            conn.disconnect();

        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Invalid URL", e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Something Wrong with the " + Client.Params.URL + " Will Not Retry", e);
        }
        return output;

    }

}

interface Client<K, V> {
    V get(K k, String... optionalParams);

    interface Params {
        // Should be read from a property file.
        String URL = "http://demo4729673.mockable.io/";
        String GET = "GET";
        String HEADER_ACCEPT = "Accept";
        String APP_JSON = "application/json";
        int READ_TIME_OUT = 1000;

        enum HTTPCODES {
            TWO_HUNDERED(200);
            private int val;

            public int getVal() {
                return val;
            }

            public void setVal(int val) {
                this.val = val;
            }

            private HTTPCODES(int val) {
                this.val = val;
            }

        }
    }
}