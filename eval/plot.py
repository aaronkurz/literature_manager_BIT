from matplotlib import pyplot as plt

values = [160.00, 122.00, 128.00, 141.00, 147.00]

# create a box plot
plt.boxplot(values)
plt.title('LLM processing time per paper (5 runs)')
plt.ylabel('Time (s)')
# make sure plot has no unnecessary whitespace left and right
plt.tight_layout()
# show the plot
plt.savefig('llm_processing_time_boxplot.png', dpi=300)