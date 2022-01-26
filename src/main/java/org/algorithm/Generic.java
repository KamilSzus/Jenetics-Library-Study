package org.algorithm;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;
import io.jenetics.BitChromosome ;
import io.jenetics.BitGene ;
import io.jenetics.Genotype;
import io.jenetics.Mutator ;

import io.jenetics.Phenotype ;
import io.jenetics.RouletteWheelSelector ;
import io.jenetics.SinglePointCrossover ;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Engine ;
import io.jenetics.engine.EvolutionStatistics ;

public class Generic {
    private static Integer count(final Genotype<BitGene> gt) {
        return gt.chromosome()
                .as(BitChromosome.class)
                .bitCount();
    }

    public static void main(String[] args) {
        //geneticAlgorithm();
    }



    private static void geneticAlgorithm() {
        final Engine<BitGene, Integer> engine = Engine
                .builder(Generic::count
                        ,BitChromosome.of(20, 0.15))//Construct a new BitChromosome with the given _length.
                        .populationSize(500)
                        //.offspringSelector(new RouletteWheelSelector<>())
                        //.survivorsSelector(new TournamentSelector<>())
                        .selector(new TournamentSelector<>()) //okreslamy jaka metoda dokonujemy podzialu
                        .alterers(new Mutator<>(0.55)//This class is for mutating a chromosomes of an given population.
                        ,new SinglePointCrossover<>(0.06))//One or two children are created by taking two parent strings and cutting them at some randomly chosen site
                        .build();
        final EvolutionStatistics<Integer, ?> statistics = EvolutionStatistics.ofNumber();

        final Phenotype<BitGene, Integer> best = engine.stream()
                .limit(bySteadyFitness(7))
                .limit(100)
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best);
    }
}