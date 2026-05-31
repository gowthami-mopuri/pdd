-- Run this script in the Supabase SQL Editor to create the tables

CREATE TABLE public.patients (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    patient_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INTEGER,
    gender VARCHAR(50),
    height INTEGER,
    weight INTEGER,
    last_visit DATE DEFAULT CURRENT_DATE,
    status VARCHAR(50) DEFAULT 'Consultation',
    risk VARCHAR(50) DEFAULT 'Pending',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- Note: We are flattening all wizard data into a JSONB column to make it incredibly flexible for AI analysis
ALTER TABLE public.patients ADD COLUMN clinical_data JSONB DEFAULT '{}'::jsonb;
