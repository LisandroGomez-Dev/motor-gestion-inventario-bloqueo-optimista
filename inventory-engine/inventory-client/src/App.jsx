import React, { useState, useEffect } from 'react';
import { Package, Cpu, RefreshCw, Zap, CheckCircle2, Layers, PlusCircle } from 'lucide-react';

// Base URL de la API Spring Boot.
// El frontend usa esta dirección para consultar y modificar el inventario.
const API_BASE = 'http://localhost:8080/api/v1/inventario';

/**
 * Componente principal de la interfaz.
 * Es la pantalla que permite ver el stock, descontarlo y simular concurrencia.
 */

export default function App() {
  // Estado para guardar la información del inventario actual.
  const [inventario, setInventario] = useState(null);

  // Indica si la pantalla está cargando datos iniciales.
  const [loading, setLoading] = useState(true);

  // Cantidad que se quiere descontar en una operación manual.
  const [cantidadDescontar, setCantidadDescontar] = useState(1);

  // Stock que se quiere fijar manualmente.
  const [nuevoStock, setNuevoStock] = useState(20);

  // Historial de eventos para mostrar el comportamiento del sistema.
  const [logs, setLogs] = useState([]);

  // Determina si una simulación concurrente está en ejecución.
  const [ejecutando, setEjecutando] = useState(false);

  /**
   * Obtiene el stock actual del producto con ID 1 desde la API.
   */
  const fetchStock = async () => {
    try {
      const res = await fetch(`${API_BASE}/1`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setInventario(data);
    } catch (err) {
      addLog(`Error al sincronizar datos: ${err.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  // Cuando el componente se monta, traemos el inventario inicial.
  useEffect(() => {
    fetchStock();
  }, []);

  /**
   * Agrega un mensaje al historial de logs.
   * @param {string} mensaje - texto que se quiere mostrar
   * @param {string} tipo - tipo de log: info, success, warning, error
   */
  const addLog = (mensaje, tipo = 'info') => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [{ id: crypto.randomUUID(), timestamp, mensaje, tipo }, ...prev]);
  };

  /**
   * Ajusta el stock directamente desde la API.
   * @param {number} unidades - nuevo valor del stock
   */
  const ajustarStock = async (unidades) => {
    try {
      const res = await fetch(`${API_BASE}/stock`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productoId: 1, nuevoStock: unidades }),
      });
      const data = await res.json();

      if (res.ok) {
        addLog(`📦 Stock actualizado manualmente a ${data.stockDisponible} unidades (Versión ${data.version})`, 'success');
        setInventario(data);
      } else {
        addLog(`Error al ajustar stock [${res.status}]: ${data.mensaje || 'Error'}`, 'error');
      }
    } catch (err) {
      addLog(`Error de conexión: ${err.message}`, 'error');
    }
  };

  /**
   * Descuenta stock a través del endpoint de negocio.
   * @param {number} cant - cantidad a descontar
   */
  const descontarStock = async (cant = 1) => {
    try {
      const res = await fetch(`${API_BASE}/descontar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productoId: 1, cantidad: cant }),
      });
      const data = await res.json();

      if (res.ok) {
        addLog(`✅ Transacción confirmada -> Quedan ${data.stockDisponible} unidades (Versión ${data.version})`, 'success');
        setInventario(data);
      } else {
        addLog(`❌ Rechazado [${res.status}]: ${data.mensaje || 'Error'}`, res.status === 409 ? 'warning' : 'error');
      }
    } catch (err) {
      addLog(`Error de conexión: ${err.message}`, 'error');
    }
  };

  /**
   * Simula 10 peticiones concurrentes al endpoint de descuento.
   * Esto ayuda a ver cómo el bloqueo optimista evita inconsistencias.
   */
  const simularConcurrenciaMasiva = async () => {
    setEjecutando(true);
    addLog('🚀 Disparando 10 peticiones simultáneas en paralelo...', 'info');

    const peticiones = Array.from({ length: 10 }, (_, i) =>
      fetch(`${API_BASE}/descontar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productoId: 1, cantidad: 1 }),
      }).then(async (res) => ({
        id: i + 1,
        status: res.status,
        data: await res.json(),
      }))
    );

    const resultados = await Promise.all(peticiones);
    let exitos = 0;
    let conflictos = 0;

    resultados.forEach((r) => {
      if (r.status === 200) {
        exitos++;
        addLog(`Hilo #${r.id}: Compra procesada. Versión incrementada a ${r.data.version}`, 'success');
      } else if (r.status === 409) {
        conflictos++;
        addLog(`Hilo #${r.id}: Colisión (@Version 409 Conflict) - Interceptado por Bloqueo Optimista`, 'warning');
      } else {
        addLog(`Hilo #${r.id}: Error [${r.status}] ${r.data.mensaje}`, 'error');
      }
    });

    addLog(`Fin del lote: ${exitos} aprobadas | ${conflictos} colisiones prevenidas`, 'info');
    await fetchStock();
    setEjecutando(false);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 flex flex-col items-center">
      <div className="max-w-4xl w-full space-y-6">
        
        {/* Encabezado principal de la app */}
        <header className="flex justify-between items-center border-b border-slate-800 pb-4">
          <div>
            <div className="flex items-center gap-2">
              <Cpu className="text-cyan-400 w-6 h-6" />
              <h1 className="text-2xl font-black tracking-tight text-white">Engine Concurrency Lab</h1>
            </div>
            <p className="text-xs text-slate-400 mt-0.5">Control de concurrencia optimista con JPA & PostgreSQL</p>
          </div>
          <button
            onClick={fetchStock}
            className="flex items-center gap-1.5 text-xs bg-slate-900 border border-slate-700 hover:border-slate-500 px-3 py-1.5 rounded-lg transition"
          >
            <RefreshCw className="w-3.5 h-3.5 text-slate-400" />
            Recargar
          </button>
        </header>

        {/* Tarjetas con información clave del inventario */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex flex-col justify-between">
            <div className="flex justify-between items-start">
              <span className="text-xs font-semibold uppercase text-slate-400">Stock Disponible</span>
              <Package className="w-5 h-5 text-emerald-400" />
            </div>
            <div className="mt-4">
              <span className="text-4xl font-black text-emerald-400 tracking-tight">
                {loading ? '--' : inventario?.stockDisponible}
              </span>
              <span className="text-xs text-slate-500 ml-2">unidades</span>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex flex-col justify-between">
            <div className="flex justify-between items-start">
              <span className="text-xs font-semibold uppercase text-slate-400">Versión de Registro</span>
              <Layers className="w-5 h-5 text-cyan-400" />
            </div>
            <div className="mt-4">
              <span className="text-4xl font-black text-cyan-400 tracking-tight">
                {loading ? '--' : `@v${inventario?.version}`}
              </span>
              <span className="text-xs text-slate-500 ml-2">JPA @Version</span>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex flex-col justify-between">
            <div className="flex justify-between items-start">
              <span className="text-xs font-semibold uppercase text-slate-400">Estado del Motor</span>
              <CheckCircle2 className="w-5 h-5 text-indigo-400" />
            </div>
            <div className="mt-4">
              <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse" />
                Protegido contra Race Conditions
              </span>
            </div>
          </div>
        </div>

        {/* Panel con acciones de manejo de stock */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          
          {/* Card para fijar o reabastecer stock manualmente */}
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-emerald-400">Reabastecer Stock</h3>
              <PlusCircle className="w-4 h-4 text-emerald-400" />
            </div>
            <div className="flex gap-2">
              <input
                type="number"
                min="0"
                value={nuevoStock}
                onChange={(e) => setNuevoStock(Math.max(0, parseInt(e.target.value) || 0))}
                className="w-20 bg-slate-950 border border-slate-700 rounded-xl px-2 py-2 text-center text-sm font-semibold focus:outline-none focus:border-emerald-500"
              />
              <button
                onClick={() => ajustarStock(nuevoStock)}
                className="flex-1 bg-emerald-600 hover:bg-emerald-500 font-semibold text-xs uppercase tracking-wider py-2 px-3 rounded-xl transition shadow-lg shadow-emerald-950"
              >
                Fijar Stock
              </button>
            </div>
            <div className="flex gap-1.5 pt-1">
              <button
                onClick={() => ajustarStock((inventario?.stockDisponible || 0) + 10)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs py-1 rounded-lg border border-slate-700 transition"
              >
                +10
              </button>
              <button
                onClick={() => ajustarStock((inventario?.stockDisponible || 0) + 50)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs py-1 rounded-lg border border-slate-700 transition"
              >
                +50
              </button>
              <button
                onClick={() => ajustarStock(20)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-amber-400 text-xs py-1 rounded-lg border border-slate-700 transition"
              >
                Reset 20
              </button>
            </div>
          </div>

          {/* Card para descontar una cantidad manual */}
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl space-y-3 flex flex-col justify-between">
            <h3 className="font-bold text-sm text-cyan-400">Operación Unitaria</h3>
            <p className="text-xs text-slate-400">Descuenta unidades y valida la versión individual.</p>
            <div className="flex gap-2">
              <input
                type="number"
                min="1"
                value={cantidadDescontar}
                onChange={(e) => setCantidadDescontar(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-20 bg-slate-950 border border-slate-700 rounded-xl px-2 py-2 text-center text-sm font-semibold focus:outline-none focus:border-cyan-500"
              />
              <button
                onClick={() => descontarStock(cantidadDescontar)}
                className="flex-1 bg-cyan-600 hover:bg-cyan-500 font-semibold text-xs uppercase tracking-wider py-2 px-3 rounded-xl transition shadow-lg shadow-cyan-950"
              >
                Descontar
              </button>
            </div>
          </div>

          {/* Card para simular concurrencia / varias compras al mismo tiempo */}
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl space-y-3 flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-rose-400">Estrés Concurrente</h3>
              <Zap className="w-4 h-4 text-rose-400" />
            </div>
            <p className="text-xs text-slate-400">10 hilos simultáneos para forzar colisiones @Version.</p>
            <button
              disabled={ejecutando}
              onClick={simularConcurrenciaMasiva}
              className="w-full bg-rose-600 hover:bg-rose-500 disabled:opacity-50 font-semibold text-xs uppercase tracking-wider py-2.5 px-3 rounded-xl transition shadow-lg shadow-rose-950"
            >
              {ejecutando ? 'Procesando colisiones...' : 'Disparar 10 peticiones'}
            </button>
          </div>

        </div>

        {/* Registro de eventos para entender qué pasó en cada operación */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-3">
          <div className="flex justify-between items-center">
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Registro de Transacciones</h4>
            <button onClick={() => setLogs([])} className="text-xs text-slate-500 hover:text-slate-300">
              Limpiar
            </button>
          </div>
          <div className="bg-slate-950 border border-slate-900 rounded-xl p-4 h-56 overflow-y-auto font-mono text-xs space-y-2">
            {logs.length === 0 && <span className="text-slate-600">// Esperando operaciones...</span>}
            {logs.map((log) => (
              <div
                key={log.id}
                className={`flex gap-2 ${
                  log.tipo === 'success'
                    ? 'text-emerald-400'
                    : log.tipo === 'warning'
                    ? 'text-amber-400 font-semibold'
                    : log.tipo === 'error'
                    ? 'text-rose-400'
                    : 'text-slate-400'
                }`}
              >
                <span className="text-slate-600">[{log.timestamp}]</span>
                <span>{log.mensaje}</span>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}

