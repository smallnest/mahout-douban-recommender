package com.colobu.douban.recommender;

import com.google.common.io.Closeables;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.iterator.FileLineIterator;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoubanUserBasedRecommender {
    public static Map<Long, String> getMovies(String base) {
        Map<Long, String> movies = new HashMap<>();
        try {
            File file = new File(base + "hot_movies.csv");
            FileLineIterator iterator = new FileLineIterator(file, false);
            String line = iterator.next();
            while (!line.isEmpty()) {
                String[] m = line.split(",");
                movies.put(Long.parseLong(m[0]), m[2]);
                line = iterator.next();
            }
            Closeables.close(iterator, true);
        } catch (Exception ex) {
        }
        return movies;
    }

    public static void main(String[] args) throws Exception {
        String base = "C:\\Users\\smallnest\\Desktop\\test\\";
        File file = new File(base + "user_movies.csv");
        DoubanFileDataModel model = new DoubanFileDataModel(file);

        //http://www.cnphp6.com/archives/84955
        //曼哈顿相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity(model);
        //欧几里德相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity(model);
        //对数似然相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity(model);
        //斯皮尔曼相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity(model);
        //Tanimoto 相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity(model)
        //Cosine相似度
        //UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity();

        //皮尔逊相似度
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

        UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);



        Recommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
        Recommender cachingRecommender = new CachingRecommender(recommender);

        //评估
        RMSRecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
        RecommenderBuilder recommenderBuilder = dataModel -> cachingRecommender;
        double score = evaluator.evaluate(recommenderBuilder, null, model, 0.95, 0.05);
        System.out.println("Score=" + score);


        Map<Long, String> movies = getMovies(base);
        for (long userID = 0; userID < 100; userID++) {
            String userName = model.userIDAndNameMapping.get(userID);
            List<RecommendedItem> recommendations = cachingRecommender.recommend(userID, 2);
            System.out.print("为用户 " + userName + " 推荐电影:");
            for (RecommendedItem recommendation : recommendations) {
                System.out.print(recommendation.getItemID() + "," + movies.get(recommendation.getItemID()) + " ");
            }
            System.out.println();
        }


        PrintWriter writer = new PrintWriter(base + "result.csv", "UTF-8");
        for (long userID = 0; userID < model.userIDAndNameMapping.size(); userID++) {
            String userName = model.userIDAndNameMapping.get(userID);
            List<RecommendedItem> recommendations = cachingRecommender.recommend(userID, 5);
            if (recommendations.size() > 0) {
                String line = userName + ",";
                for (RecommendedItem recommendation : recommendations) {
                    line += recommendation.getItemID() + ":" + movies.get(recommendation.getItemID()) + ",";
                }
                if (line.endsWith(","))
                    line = line.substring(0, line.length() - 1);

                writer.println(line);
            }
        }
        writer.close();
    }
}
