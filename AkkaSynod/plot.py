import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import os

def plot_line_diagrams(df, x_param, fixed_param, color_param, y_param, output_dir):
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

def plot_box_diagram_alpha_vs_durationMinusTimeout_fixed_n(df, output_dir):
    unique_n_values = df['n'].unique()
    for n_val in unique_n_values:
        filtered_df = df[df['n'] == n_val].copy()
        filtered_df['duration_minus_timeout'] = filtered_df['duration'] - filtered_df['timeout']
        
        plt.figure(figsize=(6, 4))
        sns.boxplot(x=filtered_df['alpha'].astype(str), y=filtered_df['duration_minus_timeout'])
        plt.title(f'alpha vs duration after timeout (n={n_val})')
        plt.xlabel('alpha')
        plt.ylabel('duration - timeout')
        plt.savefig(f"{output_dir}/alpha_vs_duration_after_timeout_n_{n_val}.png")
        plt.close()

def plot_box_diagram_alpha_vs_duration_fixed_n(df, output_dir):
    unique_n_values = df['n'].unique()
    for n_val in unique_n_values:
        filtered_df = df[df['n'] == n_val]
        
        plt.figure(figsize=(6, 4))
        sns.boxplot(x=filtered_df['alpha'].astype(str), y=filtered_df['duration'])
        plt.title(f'alpha vs duration (n={n_val})')
        plt.xlabel('alpha')
        plt.ylabel('duration')
        plt.savefig(f"{output_dir}/alpha_vs_duration_n_{n_val}.png")
        plt.close()

def plot_box_diagram_basic(df, x_param, y_param, output_dir):
    plt.figure(figsize=(6, 4))
    sns.boxplot(x=df[x_param].astype(str), y=df[y_param])
    plt.title(f'{x_param} vs {y_param}')
    plt.xlabel(x_param)
    plt.ylabel(y_param)
    plt.savefig(f"{output_dir}/{x_param}_vs_{y_param}.png")
    plt.close()

def plot_box_diagram_timeout_and_alpha_vs_duration_fixed_n(df, output_dir):
    unique_n_values = df['n'].unique()
    for n_val in unique_n_values:
        filtered_df = df[df['n'] == n_val]
        
        plt.figure(figsize=(8, 6))
        sns.boxplot(x=filtered_df['timeout'].astype(str), y=filtered_df['duration'], hue=filtered_df['alpha'].astype(str))
        plt.title(f'timeout vs duration per alpha (n={n_val})')
        plt.xlabel('timeout')
        plt.ylabel('duration')
        plt.legend(title='alpha')
        plt.savefig(f"{output_dir}/timeout_and_alpha_vs_duration_n_{n_val}.png")
        plt.close()

def plot_and_save_linediagrams(df, output_dir):
    
    os.makedirs(output_dir, exist_ok=True)
    
    plot_line_diagrams(df, 'n', 'alpha', 'timeout', 'duration', output_dir)
    plot_line_diagrams(df, 'alpha', 'n', 'timeout', 'duration', output_dir)
    plot_line_diagrams(df, 'n', 'timeout', 'alpha', 'duration', output_dir)
    plot_line_diagrams(df, 'timeout', 'n', 'alpha', 'duration', output_dir)
    plot_line_diagrams(df, 'timeout', 'alpha', 'n', 'duration', output_dir)
    plot_line_diagrams(df, 'alpha', 'timeout', 'n', 'duration', output_dir)

def plot_and_save_box(df, output_dir):

    os.makedirs(output_dir, exist_ok=True)
    
    plot_box_diagram_alpha_vs_durationMinusTimeout_fixed_n(df, output_dir)
    plot_box_diagram_alpha_vs_duration_fixed_n(df, output_dir)
    plot_box_diagram_basic(df, 'n', 'duration', output_dir)
    plot_box_diagram_basic(df, 'alpha', 'duration', output_dir)
    plot_box_diagram_basic(df, 'timeout', 'duration', output_dir)
    plot_box_diagram_timeout_and_alpha_vs_duration_fixed_n(df, output_dir)

df = pd.read_csv('data.csv')
print('linediagrams...')
plot_and_save_linediagrams(df, 'plots/linediagrams')
print('boxdiagrams...')
plot_and_save_box(df, 'plots/boxdiagrams')
print('done')
