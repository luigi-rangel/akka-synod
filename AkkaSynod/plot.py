import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os

def plot_and_save(csv_file, output_dir="plots"):
    # Read the CSV file
    df = pd.read_csv(csv_file)
    
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    unique_alpha = df['alpha'].unique()
    unique_n = df['n'].unique()
    unique_timeout = df['timeout'].unique()
    
    # Plot n vs duration for each alpha, coloring by timeout
    for alpha_val in unique_alpha:
        filtered_df = df[df['alpha'] == alpha_val]
        plt.figure(figsize=(6, 4))
        sns.scatterplot(x=filtered_df['n'].astype(str), y='duration', hue='timeout', palette='coolwarm', data=filtered_df)
        plt.title(f'n vs Duration (alpha={alpha_val})')
        plt.xlabel('n')
        plt.ylabel('Duration')
        plt.legend(title='Timeout')
        plt.savefig(f"{output_dir}/n_vs_duration_alpha_{alpha_val}.png")
        plt.close()
    
    # Plot alpha vs duration for each n, coloring by timeout
    for n_val in unique_n:
        filtered_df = df[df['n'] == n_val]
        plt.figure(figsize=(6, 4))
        sns.scatterplot(x='alpha', y='duration', hue='timeout', palette='coolwarm', data=filtered_df)
        plt.title(f'Alpha vs Duration (n={n_val})')
        plt.xlabel('Alpha')
        plt.ylabel('Duration')
        plt.legend(title='Timeout')
        plt.savefig(f"{output_dir}/alpha_vs_duration_n_{n_val}.png")
        plt.close()
    
    # Plot timeout vs duration for each n, coloring by alpha
    for n_val in unique_n:
        filtered_df = df[df['n'] == n_val]
        plt.figure(figsize=(6, 4))
        sns.scatterplot(x='timeout', y='duration', hue='alpha', palette='coolwarm', data=filtered_df)
        plt.title(f'Timeout vs Duration (n={n_val})')
        plt.xlabel('Timeout')
        plt.ylabel('Duration')
        plt.legend(title='Alpha')
        plt.savefig(f"{output_dir}/timeout_vs_duration_n_{n_val}.png")
        plt.close()

# Example usage
csv_file = 'data.csv'  # Change to your actual CSV file name
plot_and_save(csv_file)
