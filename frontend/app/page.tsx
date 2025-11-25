"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Calendar, MapPin, ArrowRight, Zap } from 'lucide-react';

type Event = {
  id: number;
  title: string;
  date: string;
  venue: { name: string; address: string; capacity: number };
};

export default function HomePage() {
  const [events, setEvents] = useState<Event[]>([]);

  useEffect(() => {
    fetch('http://localhost:8080/api/catalog/events')
      .then(res => res.json())
      .then(data => setEvents(data))
      .catch(err => console.error("Failed to fetch events", err));
  }, []);


  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-violet-500 selection:text-white">
      
      {/* Hero Section */}
      <div className="relative border-b border-slate-800 bg-[url('https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=2070&auto=format&fit=crop')] bg-cover bg-center">
        <div className="absolute inset-0 bg-slate-950/80 backdrop-blur-sm"></div>
        <div className="relative max-w-6xl mx-auto px-6 py-24">
          <div className="inline-flex items-center gap-2 text-violet-400 border border-violet-500/30 bg-violet-500/10 px-3 py-1 rounded-full text-xs font-medium mb-6">
            <Zap size={14} /> 
            <span>Live High-Concurrency Demo</span>
          </div>
          <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 tracking-tight">
            Secure your seat <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-violet-400 to-fuchsia-400">
              before it's gone.
            </span>
          </h1>
          <p className="text-xl text-slate-400 max-w-2xl leading-relaxed">
            Experience the next generation of ticketing. Powered by Redis distributed locking and Apache Kafka for ultra-low latency booking.
          </p>
        </div>
      </div>

      {/* Events Grid */}
      <main className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-2xl font-bold text-white mb-8">Upcoming Events</h2>
        
        {events.length === 0 ? (
           <div className="text-slate-500">Loading events or no events found...</div>
        ) : (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <div key={event.id} className="group relative bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden hover:border-violet-500/50 transition-all duration-300 hover:shadow-2xl hover:shadow-violet-500/10">
                
                {/* Card Header */}
                <div className={`h-32 w-full bg-gradient-to-br ${event.id % 2 === 0 ? 'from-blue-600 to-violet-600' : 'from-fuchsia-600 to-pink-600'} opacity-80 group-hover:opacity-100 transition-opacity`}></div>
                
                <div className="p-6">
                  <div className="text-xs font-medium text-violet-400 mb-2 uppercase tracking-wider">Concert</div>
                  <h3 className="text-xl font-bold text-white mb-2 group-hover:text-violet-200 transition-colors">{event.title}</h3>
                  
                  <div className="space-y-3 mt-4 mb-6">
                    <div className="flex items-center gap-3 text-sm text-slate-400">
                      <Calendar size={16} className="text-slate-500"/>
                      {new Date(event.date).toLocaleDateString()}
                    </div>
                    <div className="flex items-center gap-3 text-sm text-slate-400">
                      <MapPin size={16} className="text-slate-500"/>
                      {event.venue.name}
                    </div>
                  </div>

                  <Link 
                    href={`/event/${event.id}`} 
                    className="block w-full py-3 rounded-xl bg-slate-800 text-center font-medium text-white hover:bg-violet-600 transition-all duration-300 flex items-center justify-center gap-2 group-hover:gap-3"
                  >
                    Get Tickets <ArrowRight size={16} />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}