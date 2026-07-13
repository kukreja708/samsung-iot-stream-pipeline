import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Cpu, Thermometer, ShieldAlert, Radio } from 'lucide-react';

export default function App() {
  const [telemetryStream, setTelemetryStream] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [systemMetrics, setSystemMetrics] = useState({ avgTemp: 0, avgCpu: 0 });

  useEffect(() => {
    // Explicit connection to the full absolute URL
    const eventSource = new EventSource('http://localhost:8080/api/v1/telemetry/stream');

    eventSource.onmessage = (event) => {
      // This explicitly listens to standard event messages
      const data = JSON.parse(event.data);

      setTelemetryStream((prev) => {
        const updated = [...prev, {
          time: new Date(data.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
          cpu: data.metrics.cpuUsagePercent,
          temp: data.metrics.temperatureCelsius,
          deviceId: data.deviceId
        }];
        return updated.slice(-15);
      });

      if (data.metrics.cpuUsagePercent > 80) {
        setAlerts((prev) => [
          { id: Date.now(), msg: `🚨 [OVERHEATING] ${data.deviceId} spike at ${data.metrics.cpuUsagePercent}% CPU!` },
          ...prev.slice(0, 4)
        ]);
      }
    };

    // Add this explicit listener block to capture what the error actually says
    eventSource.addEventListener('error', (e) => {
      if (e.readyState === EventSource.CLOSED) {
        console.log("Stream closed by server. Retrying connection...");
      } else {
        console.error("Active EventSource stream error details:", e);
      }
    });

    return () => eventSource.close();
  }, []);


  return (
    <div style={{ backgroundColor: '#0f172a', color: '#f8fafc', minHeight: '100vh', padding: '24px', fontFamily: 'sans-serif' }}>
      {/* Header */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #334155', paddingBottom: '16px', marginBottom: '24px' }}>
        <h1 style={{ margin: 0, fontSize: '24px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Radio color="#38bdf8" /> Samsung IoT Device Monitoring Dashboard
        </h1>
        <span style={{ fontSize: '14px', padding: '6px 12px', borderRadius: '9999px', backgroundColor: '#1e293b', border: '1px solid #22c55e', color: '#22c55e', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#22c55e', display: 'inline-block' }}></span> LIVE STREAMING
        </span>
      </header>

      {/* Grid Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>

        {/* Left Side: Real-Time Analytics Graph */}
        <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '12px', border: '1px solid #334155' }}>
          <h2 style={{ fontSize: '18px', marginTop: 0, marginBottom: '20px' }}>Telemetry Streaming Analysis</h2>
          <div style={{ width: '100%', height: 350 }}>
            <ResponsiveContainer>
              <LineChart data={telemetryStream}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="time" stroke="#94a3b8" />
                <YAxis stroke="#94a3b8" />
                <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f8fafc' }} />
                <Line type="monotone" dataKey="cpu" name="CPU Usage %" stroke="#ef4444" strokeWidth={2} dot={false} />
                <Line type="monotone" dataKey="temp" name="Temperature °C" stroke="#38bdf8" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Right Side: Alert Terminal */}
        <div style={{ backgroundColor: '#1e293b', padding: '20px', borderRadius: '12px', border: '1px solid #334155', display: 'flex', flexDirection: 'column' }}>
          <h2 style={{ fontSize: '18px', marginTop: 0, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px', color: '#f43f5e' }}>
            <ShieldAlert /> Critical Anomalies
          </h2>
          <div style={{ flexGrow: 1, overflowY: 'auto', backgroundColor: '#0f172a', borderRadius: '8px', padding: '12px', border: '1px solid #334155' }}>
            {alerts.length === 0 ? (
              <p style={{ color: '#64748b', textAlign: 'center', marginTop: '40px' }}>No anomalies detected. System stable.</p>
            ) : (
              alerts.map(alert => (
                <div key={alert.id} style={{ padding: '8px 12px', borderLeft: '4px solid #ef4444', backgroundColor: '#1e293b', marginBottom: '8px', borderRadius: '4px', fontSize: '14px' }}>
                  {alert.msg}
                </div>
              ))
            )}
          </div>
        </div>


      </div>
    </div>
  );
}
