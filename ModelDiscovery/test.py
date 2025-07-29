import os
import csv
import pm4py
import sys 
from pprint import pprint  
from pm4py.algo.discovery.alpha import algorithm as alpha_miner
from pm4py.algo.discovery.heuristics import algorithm as heuristics_miner
from pm4py.algo.discovery.inductive import algorithm as inductive_miner
from pm4py.algo.evaluation import algorithm as evaluation
from pm4py.objects.conversion.log import converter as stream_converter
from pm4py.objects.log.importer.xes import importer as xes_import
from pm4py.visualization.petri_net import visualizer as pn_visualizer
from pm4py.algo.conformance.tokenreplay import algorithm as token_replay
from pm4py.objects.petri_net.exporter import exporter as pnml_exporter

def save_evaluation_metrics(system, iteration, noise_type, noise_level, results):
    csv_file = os.path.join(os.path.dirname(__file__), "evaluation_results_repetition.csv")
    file_exists = os.path.exists(csv_file)

    with open(csv_file, 'a', newline='') as f:
        writer = csv.writer(f)
        if not file_exists:
            writer.writerow(['System', 'Iteration', 'NoiseType', 'NoiseLevel', 'Miner', 'Precision', 'LogFitness', 'Generalization', 'Simplicity'])

        for miner, result in results.items():
            writer.writerow([
                system,
                iteration,
                noise_type,
                noise_level,
                miner,
                result['precision'],
                result['fitness']['log_fitness'],
                result['generalization'],
                result['simplicity']
            ])

def evaluate_log(log, logname, system, iteration, output_dir, noise_type, noise_level):
    # Alpha miner
    a_net, a_initial_marking, a_final_marking = alpha_miner.apply(log)
    pm4py.write_pnml(a_net, a_initial_marking, a_final_marking,
        os.path.join(output_dir, f"{os.path.splitext(logname)[0]}_alpha.pnml"))

    # Heuristics miner
    h_net, h_initial_marking, h_final_marking = heuristics_miner.apply(log)
    pm4py.write_pnml(h_net, h_initial_marking, h_final_marking,
        os.path.join(output_dir, f"{os.path.splitext(logname)[0]}_heuristics.pnml"))

    # Inductive miner
    i_tree = inductive_miner.apply(log)
    i_net, i_initial_marking, i_final_marking = pm4py.convert_to_petri_net(i_tree)
    pm4py.write_pnml(i_net, i_initial_marking, i_final_marking,
        os.path.join(output_dir, f"{os.path.splitext(logname)[0]}_inductive.pnml"))

    # Evaluate
    results = {
        'alpha': evaluation.apply(log, a_net, a_initial_marking, a_final_marking),
        'heuristic': evaluation.apply(log, h_net, h_initial_marking, h_final_marking),
        'inductive': evaluation.apply(log, i_net, i_initial_marking, i_final_marking)
    }

    save_evaluation_metrics(system, iteration, noise_type, noise_level, results)

def process_log(system, iteration, noise_type, noise_level, base_file_path):
    sys.setrecursionlimit(10000)

    log_name = f"{system}-noise-{noise_type}-{noise_level}.xes"
    log_path = os.path.join(base_file_path, "logs", str(iteration), log_name)
    output_dir = os.path.join(base_file_path, "models", str(iteration))

    log = xes_import.apply(log_path)
    log = pm4py.convert_to_event_log(log)

    evaluate_log(log, log_name, system, iteration, output_dir, noise_type, noise_level)

def main():
    # systems = ["Sepsis", "RTFMS", "BPIC2012"]
    # noise_types = ['a', 'i', 's', 'o', 'i-a-o-s']
    systems = ["BPIC2012"]
    noise_types = ['s']
    noise_levels = [1.5, 2.0]
    base_file_path = "/Users/anandik/Library/CloudStorage/OneDrive-TheUniversityofMelbourne/GR/Datasets/CaseStudyNoise/Repetition/"

    for system in systems:
        # First evaluate the clean log
        # clean_log_name = f"{system}.xes"
        # clean_log_path = os.path.join(base_file_path, "systems", clean_log_name)
        # output_dir = os.path.join(base_file_path, "systems")
        # log = xes_import.apply(clean_log_path)
        # log = pm4py.convert_to_event_log(log)
        # evaluate_log(log, clean_log_name, system, "n/a", output_dir, "n/a", 0.0)

        for iteration in range(1, 2):
            for noise_type in noise_types:
                for noise_level in noise_levels:
                    print(f"\nProcessing {system} | Iteration {iteration} | Noise: {noise_type} | Level: {noise_level}")
                    process_log(system, iteration, noise_type, noise_level, base_file_path)

if __name__ == "__main__":
    main()