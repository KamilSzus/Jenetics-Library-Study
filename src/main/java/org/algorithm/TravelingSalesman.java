package org.algorithm;

import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.PartiallyMatchedCrossover;
import io.jenetics.Phenotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Constraint;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Evolution;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.hypot;
import static java.lang.Math.sin;
import static java.util.Objects.requireNonNull;

public class TravelingSalesman implements Problem<ISeq<double[]>, EnumGene<double[]>,Double>
{
    private  final ISeq<double[]> _points;

    public TravelingSalesman(ISeq<double[]> points) {
        _points = requireNonNull(points);
    }

    @Override
    public Function<ISeq<double[]>, Double> fitness() {
        return p -> IntStream.range(0,p.length())
                .mapToDouble(i ->{
                    final  double[] p1 = p.get(i);
                    final  double[] p2 = p.get((i+1)% p.size());
                    return  hypot(p1[0] - p2[0],p1[1] - p2[1]);})
                    .sum();
    }

    @Override
    public Codec<ISeq<double[]>, EnumGene<double[]>> codec() {
        return Codecs.ofPermutation(_points);
    }
    public  static TravelingSalesman of(int stops, double radius){
        final MSeq<double[]> points = MSeq.ofLength(stops);
        final double delta = 2.0 * PI /stops;

        for(int i = 0; i<stops; ++i){
            final double alpha = delta*i;
            final double x = cos(alpha) * radius + radius;
            final double y = sin(alpha) * radius + radius;
            points.set(i,new double[]{x,y});
        }

        final Random random = RandomRegistry.random();
        for (int j = points.length() - 1; j > 0; --j){
            final int i = random.nextInt(j+1);
            final double[] tmp = points.get(i);
            points.set(i,points.get(j));
            points.set(j,tmp);
        }
        return  new TravelingSalesman(points.toISeq());
    }

    public static void main(String[] args){
        int stops = 20;
        double R = 10;
        double minPathLength = 2.0 * stops * R * sin(PI/stops);

        TravelingSalesman tsm = TravelingSalesman.of(stops,R);
        Engine<EnumGene<double[]>, Double> engine = Engine
                .builder(tsm)
                .optimize(Optimize.MINIMUM)
                .maximalPhenotypeAge(11)
                .populationSize(500)
                .alterers(
                        new SwapMutator<>(0.2),
                        new PartiallyMatchedCrossover<>(0.35))
                .build();
        EvolutionStatistics<Double,?> statistics = EvolutionStatistics.ofNumber();

        Phenotype<EnumGene<double[]>,Double> best = engine.stream()
                .limit(bySteadyFitness(25))
                .limit(250)
                .peek(statistics)
                .collect(toBestPhenotype());
        System.out.println(statistics);
        System.out.println("know min = " + minPathLength);
        System.out.println("Found miin = " + best.fitness());
    }
}
