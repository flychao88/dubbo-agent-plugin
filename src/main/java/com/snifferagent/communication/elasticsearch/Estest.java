package com.snifferagent.communication.elasticsearch;



import java.net.InetAddress;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static org.elasticsearch.index.query.QueryBuilders.templateQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;


/**
 * Date:2017/11/29
 *
 * @author:chao.cheng
 **/
public class Estest {
    Client client;


    public Estest() throws Exception {

         client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("103.240.17.228"), 9300));
    }

    public void get() {

       /* GetResponse getResponse = client.prepareGet(index, type, "1").get();
        System.out.println(getResponse.getSourceAsString());*/


       String json = " {\"traceId\":\"35bbf0b8f58cb0\",\"levelInfoData\":{\"spanId\":\"1\",\"log\":\"null\",\"levelId\":\"2\",\"execTime\":\"259\",\"methodName\":\"getString\",\"startTime\":\"1512481633534\",\"className\":\"org.spring.springboot.dubbo.CityDubboService\",\"endTime\":\"1512481633793\"}}";

        client.prepareIndex("dmtest8", "tweet", "35bbf0b8f58cb0").setSource(json).execute().actionGet();


        QueryBuilder qb = QueryBuilders.matchAllQuery();
        SearchResponse searchResponse = client.prepareSearch("dmtest8")
                .setTypes("tweet")
                .setSearchType(SearchType.SCAN)
                .setSize(10).setQuery(qb)
                .setScroll(new TimeValue(6000))
                .execute()
                .actionGet();

      //  QueryBuilder qb1 = termQuery("traceId", "35b88b44bf2fd0");

       // client.prepareIndex("dmtest7", "tweet", "1").setSource(json).execute().actionGet();

        /*try {
            Map<String,Object> infoDataMap = new HashMap();
            Map<String,Object> spanMap = new HashMap();
            Map<String,Object> levelMap = new HashMap<>();
            spanMap.put("spanId",2);
            spanMap.put("methodName","getExec");
            levelMap.put("span2", spanMap);
            infoDataMap.put("2" ,levelMap);
            client.prepareUpdate("dmtest8", "tweet" ,"1").setDoc(XContentFactory.jsonBuilder().startObject()
                    .field("traceId", "35babeaccd40f0")
                    .field("levelInfoData", infoDataMap)
                   ).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/




      //  client.prepareIndex("dmtest6", "tweet", "1").setSource(json1).execute().actionGet();
     /*  try {
           long count = client.prepareCount("dmtest6").execute().get().getCount();
           System.out.println(count+"===");
       } catch (Exception e) {
           e.printStackTrace();

       }*/


   /*  QueryBuilder qb = QueryBuilders.matchAllQuery();
        SearchResponse searchResponse = client.prepareSearch("dmtest7")
                .setTypes("tweet")
                .setSearchType(SearchType.SCAN)
                .setSize(10).setQuery(qb)
                .setScroll(new TimeValue(6000))
                .execute()
                .actionGet();

        searchResponse= client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(6000)).execute().actionGet();

        System.out.println(searchResponse.getHits().getHits().length);

        SearchHits hits = searchResponse.getHits();
        for(SearchHit hit:hits) {
            Map<String,Object> map = hit.getSource();
            System.out.println(map);
            HashMap<String,Object> tree = (HashMap)map.get("levelInfoData");
            for (Map.Entry<String, Object> entry : tree.entrySet()) {
                System.out.println(entry.getKey());
                if(entry.getValue() != null) {
                    System.out.println(entry.getValue());
                }

            }

        }*/
/*

        SearchResponse response = client.prepareSearch("dmtest7").setTypes("tweet")
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort("levelInfoData", SortOrder.ASC)
                .execute().actionGet();


        for (SearchHit searchHit : response.getHits()) {
            System.out.println(searchHit);
        }
*/



       /* QueryBuilder qb1 = termQuery("traceId", "35b981a69cabf4");

       SearchResponse response = client.prepareSearch("dmtest3").setTypes("tweet").setQuery(qb1).execute()
                .actionGet();
*/

       /*SearchResponse response = client.prepareSearch("dmtest3").setTypes("tweet").setQuery(qb1).execute()
                .actionGet();

        System.out.println(response.getHits().getTotalHits());

        SearchHits hits = response.getHits();
        for(SearchHit hit:hits) {
            System.out.println(hit.getSource());
        }
*/

        /*System.out.println(response.getHits().getTotalHits());

        SearchHits hits = response.getHits();
        for(SearchHit hit:hits) {
            TreeMap ss = (TreeMap)hit.getSource();

            System.out.println(ss);



        }*/
    }

    public static void main(String[] args) throws Exception {
        Estest test = new Estest();
        test.get();
    }

}
