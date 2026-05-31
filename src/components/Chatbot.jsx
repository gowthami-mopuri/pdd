import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, User } from 'lucide-react';
import './Chatbot.css';

const Chatbot = ({ patients, selectedPatient }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'assistant', content: 'Hello! I am your AI Dental Assistant. How can I help you analyze the patient today?' }
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };
  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping, isOpen]);

  const handleSend = async () => {
    if (!input.trim()) return;
    
    const userMsg = { role: 'user', content: input.trim() };
    const newMessages = [...messages, userMsg];
    setMessages(newMessages);
    setInput('');
    setIsTyping(true);

    const fullPatientData = patients.find(p => p.id?.toString() === selectedPatient?.toString()) || { name: 'No Patient Selected (Tell the user to select a patient from the dropdown)' };

    try {
      const response = await fetch('http://localhost:8000/chat/personalized', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          patient_data: fullPatientData,
          messages: newMessages
        })
      });

      if (!response.ok) throw new Error('API Error');

      const data = await response.json();
      setMessages([...newMessages, { role: 'assistant', content: data.reply }]);
    } catch (err) {
      setMessages([...newMessages, { role: 'assistant', content: 'Sorry, I am having trouble connecting to the AI backend.' }]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className="chatbot-widget">
      {isOpen && (
        <div className="chatbot-window">
          <div className="chatbot-header">
            <h3><MessageSquare size={18}/> AI Assistant</h3>
            <button onClick={() => setIsOpen(false)} style={{background:'transparent', border:'none', color:'white', cursor:'pointer'}}><X size={20}/></button>
          </div>
          
          <div className="chatbot-messages">
            {messages.map((msg, idx) => (
              <div key={idx} className={`chat-bubble chat-${msg.role}`}>
                {msg.content}
              </div>
            ))}
            {isTyping && (
              <div className="chat-bubble chat-assistant">
                <span className="dot-pulse">typing...</span>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="chatbot-input">
            <input 
              type="text" 
              placeholder="Ask about the patient..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            />
            <button onClick={handleSend} disabled={isTyping}><Send size={16}/></button>
          </div>
        </div>
      )}

      {!isOpen && (
        <button className="chatbot-button" onClick={() => setIsOpen(true)}>
          <MessageSquare size={28}/>
        </button>
      )}
    </div>
  );
};

export default Chatbot;
