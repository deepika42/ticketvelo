"use client";

import React, { useState, useEffect } from 'react';
import { CheckCircle, MapPin, AlertCircle, Calendar, ArrowLeft, Info } from 'lucide-react';
import Link from 'next/link';
import { useParams } from 'next/navigation';

type Seat = { id: number; rowNumber: string; seatNumber: number; section: string };
type Ticket = { id: number; status: string; seat: Seat };
type Venue = { name: string; address: string };
type EventData = { id: number; title: string; date: string; venue: Venue };

export default function EventBookingPage() {
  const params = useParams();
  const eventId = Number(params.id);

  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [eventInfo, setEventInfo] = useState<EventData | null>(null);
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const [bookingStatus, setBookingStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  
  // AUTH STATE
  const [token, setToken] = useState<string | null>(null);
  const [userId, setUserId] = useState<string>('...');

  // 1. AUTO-LOGIN ON LOAD (The Security Handshake)
  useEffect(() => {
    const initializeSession = async () => {
        // A. Check if we already have a passport in the browser
        const storedToken = localStorage.getItem('ticketvelo_token');
        const storedUserId = localStorage.getItem('ticketvelo_userid');

        if (storedToken && storedUserId) {
            setToken(storedToken);
            setUserId(storedUserId);
            console.log("🔐 Restored Session for User:", storedUserId);
            return; // Stop here, don't create a new user!
        }

        // B. No passport found? Go get a new one.
        try {
            const res = await fetch('http://localhost:8080/api/auth/login-as-guest', { method: 'POST' });
            const data = await res.json();
            
            // Save to State
            setToken(data.token);
            setUserId(data.userId);
            
            // Save to Browser Storage (So it survives refresh)
            localStorage.setItem('ticketvelo_token', data.token);
            localStorage.setItem('ticketvelo_userid', data.userId);
            
            console.log("🆕 New Guest Session Created:", data.userId);
        } catch (err) {
            console.error("Login failed", err);
            setErrorMessage("Authentication failed. Please refresh.");
        }
    };

    initializeSession();
  }, []);

  // 2. LOAD EVENT DATA
  useEffect(() => {
    const loadData = async () => {
        try {
            const eventRes = await fetch(`http://localhost:8080/api/catalog/events/${eventId}`);
            if (eventRes.ok) setEventInfo(await eventRes.json());

            const ticketRes = await fetch(`http://localhost:8080/api/bookings/event/${eventId}`);
            if (ticketRes.ok) setTickets(await ticketRes.json());
        } catch (err) {
            console.error("Connection failed", err);
        }
    };
    if (eventId) loadData();
  }, [eventId]);

  const getRows = () => {
    const rows: Record<string, Ticket[]> = {};
    tickets.forEach(t => {
        if (!rows[t.seat.rowNumber]) rows[t.seat.rowNumber] = [];
        rows[t.seat.rowNumber].push(t);
    });
    Object.keys(rows).forEach(key => rows[key].sort((a, b) => a.seat.seatNumber - b.seat.seatNumber));
    return rows;
  };

  const toggleSeat = (seatId: number) => {
    setBookingStatus('idle');
    if (selectedSeatIds.includes(seatId)) {
        setSelectedSeatIds(selectedSeatIds.filter(id => id !== seatId));
    } else {
        setSelectedSeatIds([...selectedSeatIds, seatId]);
    }
  };

  // 3. SECURE BOOKING (Sending the Header)
  const handleBook = async () => {
    if (selectedSeatIds.length === 0 || !token) return;
    setBookingStatus('loading');
    
    try {
      const response = await fetch('http://localhost:8080/api/bookings', {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          eventId: eventId,
          seatIds: selectedSeatIds
        }),
      });

      // --- SELF-HEALING LOGIC START ---
      if (response.status === 403 || response.status === 401) {
          console.warn("⚠️ Session expired or invalid. Logging out...");
          // 1. Wipe the bad credentials
          localStorage.removeItem('ticketvelo_token');
          localStorage.removeItem('ticketvelo_userid');
          // 2. Force a reload to get a fresh Guest ID
          window.location.reload();
          return;
      }
      // --- SELF-HEALING LOGIC END ---

      if (!response.ok) {
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            const errorData = await response.json();
            throw new Error(errorData.detail || 'Booking Failed');
        } else {
            throw new Error(`Server Error: ${response.status}`);
        }
      }

      setBookingStatus('success');
      const ticketRes = await fetch(`http://localhost:8080/api/bookings/event/${eventId}`);
      if (ticketRes.ok) setTickets(await ticketRes.json());
      setSelectedSeatIds([]);
    } catch (err: any) {
      setBookingStatus('error');
      setErrorMessage(err.message || 'One of these seats was just taken!');
    }
  };

  if (!eventInfo) return <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">Loading Event...</div>;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans flex flex-col">
      
      {/* Sticky Header */}
      <header className="bg-slate-950/80 backdrop-blur-md border-b border-slate-800 p-4 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Link href="/" className="p-2 hover:bg-slate-800 rounded-full transition"><ArrowLeft size={20}/></Link>
            <div>
              <h1 className="text-lg font-bold text-white">{eventInfo.title}</h1>
              <div className="text-xs text-slate-400 flex gap-2">
                  <Calendar size={12} /> <span>{new Date(eventInfo.date).toLocaleDateString()}</span>
                  <span className="text-slate-600">|</span>
                  <MapPin size={12} /> <span>{eventInfo.venue?.name}</span>
              </div>
            </div>
          </div>
          
          {/* Action Bar */}
          <div className="flex items-center gap-4">
             <div className="text-right hidden sm:block">
                <div className="text-xs text-slate-500">Guest ID: {userId}</div>
                <div className="font-bold text-violet-400">${selectedSeatIds.length * 50}</div>
             </div>
             <button
                onClick={handleBook}
                disabled={selectedSeatIds.length === 0 || bookingStatus === 'loading' || !token}
                className={`
                    px-6 py-2 rounded-full font-bold text-sm transition-all
                    ${selectedSeatIds.length === 0 
                        ? 'bg-slate-800 text-slate-500 cursor-not-allowed' 
                        : bookingStatus === 'success' 
                            ? 'bg-emerald-500 text-slate-900' 
                            : 'bg-white text-slate-900 hover:bg-violet-200'}
                `}
            >
                {bookingStatus === 'loading' ? 'Processing...' : 
                 bookingStatus === 'success' ? 'Confirmed!' : 
                 `Checkout (${selectedSeatIds.length})`}
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 p-6 overflow-hidden flex flex-col">
          
          {/* Legend */}
          <div className="flex justify-center gap-6 text-xs text-slate-400 mb-8">
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-emerald-500"></div> Available</div>
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-slate-800 border border-slate-700"></div> Booked</div>
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-violet-500 shadow-lg shadow-violet-500/50"></div> Selected</div>
          </div>

          {/* SCROLLABLE SEAT MAP CONTAINER */}
          <div className="flex-1 relative border border-slate-800 rounded-3xl bg-slate-900/50 overflow-hidden shadow-inner">
            
            <div className="absolute top-0 left-0 right-0 h-16 bg-gradient-to-b from-violet-900/20 to-transparent z-10 pointer-events-none flex justify-center pt-4">
                <span className="text-violet-500/50 text-[10px] tracking-[0.5em] uppercase font-bold">Stage</span>
            </div>

            <div className="absolute inset-0 overflow-auto p-12 scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent">
                <div className="min-w-max mx-auto"> 
                    
                    {/* Seat Grid */}
                    <div className="flex flex-col gap-2 items-center">
                        {Object.entries(getRows()).sort().map(([rowLabel, rowTickets]) => (
                            <div key={rowLabel} className="flex items-center gap-3">
                                <div className="w-6 text-xs text-slate-500 font-mono text-right sticky left-0">{rowLabel}</div>
                                
                                <div className="flex gap-1.5">
                                    {rowTickets.map(ticket => {
                                        const isBooked = ticket.status === 'BOOKED';
                                        const isSelected = selectedSeatIds.includes(ticket.seat.id);
                                        return (
                                            <button
                                                key={ticket.id}
                                                disabled={isBooked}
                                                onClick={() => toggleSeat(ticket.seat.id)}
                                                className={`
                                                    w-7 h-7 rounded-sm text-[9px] font-medium transition-all duration-150 flex items-center justify-center
                                                    ${isBooked 
                                                        ? 'bg-slate-800 text-slate-600 cursor-not-allowed border border-slate-700/50' 
                                                        : isSelected 
                                                            ? 'bg-violet-500 text-white shadow-[0_0_10px_rgba(139,92,246,0.6)] transform scale-110 z-10 ring-1 ring-white/20' 
                                                            : 'bg-emerald-500/90 hover:bg-emerald-400 text-emerald-950 hover:scale-105'
                                                    }
                                                `}
                                                title={`Row ${rowLabel} Seat ${ticket.seat.seatNumber}`}
                                            >
                                                {isBooked ? '' : ticket.seat.seatNumber}
                                            </button>
                                        );
                                    })}
                                </div>
                                
                                <div className="w-6 text-xs text-slate-500 font-mono text-left">{rowLabel}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
          </div>

          {bookingStatus === 'error' && (
            <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex items-center gap-2 text-white bg-red-500/90 backdrop-blur px-6 py-3 rounded-full shadow-xl animate-bounce">
                <AlertCircle size={18} /> {errorMessage}
            </div>
          )}
      </main>
    </div>
  );
}