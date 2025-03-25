import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os

def plot_parameter_vs_duration(df, x_param, fixed_param, color_param, y_param, output_dir):
    unique_fixed_values = df[fixed_param].unique()
    for fixed_val in unique_fixed_values:
        filtered_df = df[df[fixed_param] == fixed_val]
        avg_df = filtered_df.groupby([x_param, color_param], as_index=False)[y_param].mean()
        
        plt.figure(figsize=(6, 4))
        sns.scatterplot(x=filtered_df[x_param].astype(str), y=y_param, hue=color_param, palette='tab10', data=filtered_df, alpha=0.6)
        sns.lineplot(x=avg_df[x_param].astype(str), y=y_param, hue=avg_df[color_param], palette='tab10', data=avg_df, legend=False)
        plt.title(f'{x_param} vs {y_param} ({fixed_param}={fixed_val})')
        plt.xlabel(x_param)
        plt.ylabel(y_param)
        plt.legend(title=color_param)
        plt.savefig(f"{output_dir}/{x_param}_vs_{y_param}_{fixed_param}_{fixed_val}.png")
        plt.close()

def plot_and_save(csv_file, output_dir):
    df = pd.read_csv(csv_file)
    os.makedirs(output_dir, exist_ok=True)
    
    plot_parameter_vs_duration(df, 'n', 'alpha', 'timeout', 'duration', output_dir)
    plot_parameter_vs_duration(df, 'alpha', 'n', 'timeout', 'duration', output_dir)
    plot_parameter_vs_duration(df, 'n', 'timeout', 'alpha', 'duration', output_dir)
    plot_parameter_vs_duration(df, 'timeout', 'n', 'alpha', 'duration', output_dir)
    plot_parameter_vs_duration(df, 'timeout', 'alpha', 'n', 'duration', output_dir)
    plot_parameter_vs_duration(df, 'alpha', 'timeout', 'n', 'duration', output_dir)

plot_and_save('data.csv', 'plots')
