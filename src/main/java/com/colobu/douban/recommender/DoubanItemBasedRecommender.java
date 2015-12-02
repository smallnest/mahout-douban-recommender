package com.colobu.douban.recommender;

import com.google.common.io.Closeables;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.precompute.FileSimilarItemsWriter;
import org.apache.mahout.cf.taste.impl.similarity.precompute.MultithreadedBatchItemSimilarities;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.precompute.BatchItemSimilarities;
import org.apache.mahout.common.iterator.FileLineIterator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DoubanItemBasedRecommender {
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
        ItemSimilarity similarity = new PearsonCorrelationSimilarity(model);
        ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);

        BatchItemSimilarities batch = new MultithreadedBatchItemSimilarities(recommender, 5);
        int numSimilarities = batch.computeItemSimilarities(Runtime.getRuntime().availableProcessors(), 1, new FileSimilarItemsWriter(new File(base + "item_result.csv")));

        System.out.println("Computed " + numSimilarities + " similarities for " + model.getNumItems() + " items " + "and saved them to file " + base + "item_result.csv");
    }
}
