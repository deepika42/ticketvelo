"use client";

import React, { useState, useEffect } from 'react';
import { CheckCircle, MapPin, AlertCircle } from 'lucide-react';

// --- TYPES ---
type Seat = { id: number; rowNumber: string; seatNumber: number; section: string };
type Ticket = { id: number; status: string; seat: Seat };

export default function BookingPage() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [selectedSeatId, setSelectedSeatId] = useState<number | null>(null);
  const [bookingStatus, setBookingStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  
  // --- FIX STARTS HERE ---
  // 1. Initialize with a static value (0) to match server and client initially
  const [userId, setUserId] = useState<number>(0);

  // 2. Generate the random ID only once the browser has loaded (useEffect)
  useEffect(() => {
    setUserId(Math.floor(Math.random() * 10000));
  }, []);
  // --- FIX ENDS HERE ---

  const EVENT_ID = 1;

  useEffect(() => {
    fetchTickets();
  }, []);

  const fetchTickets = async () => {
    try {
      const res = await fetch(`http://localhost:8080/api/bookings/event/${EVENT_ID}`);
      const data = await res.json();
      const sortedData = data.sort((a: Ticket, b: Ticket) => a.seat.id - b.seat.id);
      setTickets(sortedData);
    } catch (err) {
      console.error("Failed to connect to Java Backend:", err);
    }
  };

  const handleBook = async () => {
    if (!selectedSeatId) return;
    setBookingStatus('loading');
    setErrorMessage('');
    
    try {
      const response = await fetch('http://localhost:8080/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          eventId: EVENT_ID,
          seatId: selectedSeatId,
          userId: userId // Use the state variable here
        }),
      });

      if (!response.ok) {
        throw new Error('Seat already taken!');
      }

      setBookingStatus('success');
      fetchTickets(); 
    } catch (err) {
      setBookingStatus('error');
      setErrorMessage('Booking Failed: Someone took this seat!');
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 font-sans selection:bg-violet-500 selection:text-white">
      
      {/* Header */}
      <header className="bg-slate-950 border-b border-slate-800 p-6 shadow-2xl">
        <div className="max-w-4xl mx-auto flex justify-between items-center">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-violet-400 to-fuchsia-400 bg-clip-text text-transparent">
            TicketVelo
          </h1>
          <div className="text-right hidden md:block text-xs text-slate-500">
             {/* Only show User ID if it's generated, otherwise 'Loading...' */}
             User ID: {userId > 0 ? userId : '...'}
          </div>
        </div>
      </header>

      <main className="max-w-4xl mx-auto p-6 mt-8 grid md:grid-cols-3 gap-8">
        
        {/* Event Info */}
        <div className="md:col-span-1 space-y-6">
          <div className="bg-slate-800/50 p-6 rounded-2xl border border-slate-700 backdrop-blur-sm">
            <h2 className="text-xl font-semibold text-white mb-4">Event Details</h2>
            <div className="space-y-4">
              <div>
                <div className="text-slate-400 text-xs uppercase tracking-wider mb-1">Artist</div>
                <div className="text-lg font-medium text-violet-200">Java Concurrency Masterclass</div>
              </div>
              <div className="flex gap-3 items-center">
                 <MapPin className="text-slate-500" size={18} />
                 <span className="text-sm">Madison Square Garden</span>
              </div>
            </div>
          </div>
          
          {/* Legend */}
          <div className="grid grid-cols-2 gap-3 text-xs text-slate-400">
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-emerald-500"></div> Available</div>
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-slate-700"></div> Booked</div>
             <div className="flex items-center gap-2"><div className="w-3 h-3 rounded bg-violet-500"></div> Selected</div>
          </div>
        </div>

        {/* Seat Map */}
        <div className="md:col-span-2">
          
          {/* Stage */}
          <div className="w-full h-8 bg-gradient-to-b from-violet-600/20 to-transparent border-t-2 border-violet-600 rounded-t-full mb-8 flex items-center justify-center">
            <span className="text-violet-400 text-[10px] tracking-[0.3em] uppercase">Stage</span>
          </div>

          {/* The Real Grid */}
          <div className="grid grid-cols-10 gap-2 mx-auto w-fit">
            {tickets.map((ticket) => {
                const isBooked = ticket.status === 'BOOKED';
                const isSelected = selectedSeatId === ticket.seat.id;

                return (
                    <button
                        key={ticket.id}
                        disabled={isBooked}
                        onClick={() => {
                            setSelectedSeatId(ticket.seat.id);
                            setBookingStatus('idle'); 
                        }}
                        className={`
                            w-8 h-8 rounded-sm text-[10px] font-medium transition-all duration-200 relative
                            ${isBooked 
                                ? 'bg-slate-800 text-slate-600 cursor-not-allowed' 
                                : isSelected 
                                    ? 'bg-violet-600 text-white shadow-lg scale-110 z-10' 
                                    : 'bg-emerald-500/80 hover:bg-emerald-400 text-emerald-900'
                            }
                        `}
                    >
                        {isBooked ? 'X' : ticket.seat.seatNumber}
                    </button>
                );
            })}
          </div>

          {/* Checkout Button */}
          <div className="mt-8 flex flex-col items-center gap-4">
            
            {bookingStatus === 'error' && (
                <div className="flex items-center gap-2 text-red-400 bg-red-900/20 px-4 py-2 rounded-lg text-sm border border-red-900/50">
                    <AlertCircle size={16} /> {errorMessage}
                </div>
            )}

            <button
                onClick={handleBook}
                disabled={!selectedSeatId || bookingStatus === 'loading' || bookingStatus === 'success'}
                className={`
                    px-8 py-3 rounded-full font-bold text-sm tracking-wide transition-all duration-300 flex items-center gap-2
                    ${!selectedSeatId 
                        ? 'bg-slate-800 text-slate-500 cursor-not-allowed' 
                        : bookingStatus === 'success'
                            ? 'bg-emerald-500 text-slate-900 ring-4 ring-emerald-500/20'
                            : 'bg-white text-slate-900 hover:bg-violet-50 hover:scale-105 shadow-xl'
                    }
                `}
            >
                {bookingStatus === 'loading' && <span className="animate-spin">⏳</span>}
                {bookingStatus === 'success' && <CheckCircle size={18} />}
                
                {bookingStatus === 'idle' && 'CONFIRM TICKET'}
                {bookingStatus === 'loading' && 'PROCESSING...'}
                {bookingStatus === 'success' && 'BOOKED!'}
            </button>
          </div>

        </div>
      </main>
    </div>
  );
}