<!-- Neo4jView.vue -->
<template>
  <div style="padding: 24px;">
    <p>
      The Neo4j Browser cannot be embedded in an iframe for security reasons.
      Use the button below to open it in a new tab.
    </p>
    <p><strong>URL:</strong> <a href="http://localhost:7474/browser/" target="_blank" rel="noopener">http://localhost:7474/browser/</a></p>
    <p><strong>Credentials (prototype):</strong> username: <code>neo4j</code>, password: <code>12345678</code></p>
    <button @click="openNeo4j" style="padding: 8px 12px;">Open Neo4j Browser</button>
  </div>
</template>

<script>
export default {
  methods: {
    openNeo4j() {
      window.open('http://localhost:7474/browser/', '_blank', 'noopener');
    }
  }
}
</script>