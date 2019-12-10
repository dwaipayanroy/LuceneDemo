/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package searcher;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 *
 * @author dwaipayan
 */
public class KLDivergenceSimilarity extends LMSimilarity{

    private final float mu;

    // + initialize the parameters with specified values
    public KLDivergenceSimilarity(float mu, CollectionModel collectionModel) {
        super(collectionModel);
        this.mu = mu;
    }

    public KLDivergenceSimilarity(float mu) {
        this.mu = mu;
    }
    // - initialize the parameters with specified values

    // + initialize the parameters with default values
    public KLDivergenceSimilarity(CollectionModel collectionModel) {
        super(collectionModel);
        this.mu = 2500;
    }

    public KLDivergenceSimilarity() {
        this.mu = 2500;
    }
    // - initialize the parameters with default values

    
    @Override
    public String getName() {
        return String.format("KL-Div(%f)", getMu());
    }

    @Override
    protected float score(BasicStats stats, float freq, float docLen) {
        float score = 
            (float)(
                Math.log(1 + (freq / mu * ((LMStats)stats).getCollectionProbability()))
                +
                Math.log(mu/(mu + docLen))
            );

        return score; // > 0.0f ? score : 0.0f;    
    }

    private Object[] getMu() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
