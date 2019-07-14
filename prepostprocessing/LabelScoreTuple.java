import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LabelScoreTuple<R, S>
{

private R labelName;
private S labelScore;


public LabelScoreTuple(R r, S s)
{
    this.labelName = r;
    this.labelScore = s;
}

public static void main(String[] args)
{
    List<LabelScoreTuple<String, Double>> tuples = new ArrayList<LabelScoreTuple<String, Double>>();
    // insert elements in no special order
    tuples.add(new LabelScoreTuple<String, Double>("1234", new Double(8.0)));
    tuples.add(new LabelScoreTuple<String, Double>("2341", new Double(2.0)));
    tuples.add(new LabelScoreTuple<String, Double>("23232", new Double(3.0)));

    Comparator<LabelScoreTuple<String, Double>> comparator = new Comparator<LabelScoreTuple<String, Double>>()
    {

        public int compare(LabelScoreTuple<String, Double> tupleA,
        		LabelScoreTuple<String, Double> tupleB)
        {
            return tupleA.getScore().compareTo(tupleB.getScore());
        }

    };

    Collections.sort(tuples, comparator);

    for (LabelScoreTuple<String, Double> tuple : tuples)
    {
        System.out.println(tuple.getLabelName() + " -> " + tuple.getScore());
    }

}

public R getLabelName()
{
    return labelName;
}

public void setName(R name)
{
    this.labelName = name;
}

public S getScore()
{
    return labelScore;
}

public void setData(S score)
{
    this.labelScore = score;
}

}
