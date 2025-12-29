from matplotlib import pyplot as plt

# create a piechart with 80% green (correct), 10% orange (missed), 10% blue (sytax)

labels = ['Correct', 'Missed', 'Syntax Error']
sizes = [8, 1, 1]
colors = ['#4CAF50', '#FF9800', '#2196F3']
plt.pie(sizes, labels=labels, colors=colors, autopct='%1.1f%%', startangle=140)
plt.title('LLM Classification Performance')

# the areas should show 8, 1, 1 instead of percentages
plt.axis('equal')  # Equal aspect ratio ensures that pie is drawn as a circle.
# make sure plot has no unnecessary whitespace left and right
plt.tight_layout()
# show the plot
plt.savefig('llm_classification_performance_piechart.png', dpi=300)