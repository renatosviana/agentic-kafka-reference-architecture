// account-ui/src/App.jsx
import { useState } from "react";
import axios from "axios";

function App() {
  const [accountId, setAccountId] = useState("ACC123");
  const [summaries, setSummaries] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadSummaries = async () => {
    setLoading(true);
    try {
      const res = await axios.get(
        `http://localhost:8080/summaries/${accountId}`
      );
      setSummaries(res.data);
    } catch (e) {
      console.error(e);
      alert("Failed to load summaries");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: "2rem auto", fontFamily: "sans-serif" }}>
      <h1>GenAI Kafka – Account Summaries</h1>

      <div style={{ marginBottom: "1rem" }}>
        <input
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          placeholder="Account ID"
          style={{ padding: "0.5rem", width: 200, marginRight: 8 }}
        />
        <button onClick={loadSummaries} disabled={loading}>
          {loading ? "Loading..." : "Load summaries"}
        </button>
      </div>

      <ul>
        {summaries.map((s) => (
          <li key={s.id} style={{ marginBottom: "1rem", borderBottom: "1px solid #ddd", paddingBottom: "0.5rem" }}>
            <div><strong>Created:</strong> {s.createdAt}</div>
            <div><strong>Classification:</strong> {s.classification} (risk: {s.riskScore})</div>
            <div><strong>Summary:</strong> {s.summary}</div>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;
