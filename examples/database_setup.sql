-- =====================================================
-- Database Setup: Calamiteitenbeheer
-- FME Workspace: Brandweer en Noodsteunpunten
-- =====================================================

-- Create schema
CREATE SCHEMA IF NOT EXISTS calamiteiten;

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_topology;

-- =====================================================
-- Table: brandweer_kazernes
-- =====================================================

CREATE TABLE IF NOT EXISTS calamiteiten.brandweer_kazernes (
    id SERIAL PRIMARY KEY,
    naam VARCHAR(255) NOT NULL,
    adres VARCHAR(255),
    plaats VARCHAR(100),
    postcode VARCHAR(10),
    telefoonnummer VARCHAR(20),
    email VARCHAR(100),
    type_kazerne VARCHAR(50) CHECK (type_kazerne IN ('Beroeps', 'Vrijwillig', 'Gemengd')),
    veiligheidsregio VARCHAR(100),
    aantal_voertuigen INTEGER CHECK (aantal_voertuigen >= 0),
    bemanning_24_7 BOOLEAN DEFAULT false,
    specialisaties TEXT[], -- Array: ['Hoogwerker', 'Duikteam', 'Chemisch']
    opkomsttijd_minuten INTEGER CHECK (opkomsttijd_minuten > 0),
    website VARCHAR(255),
    opmerkingen TEXT,
    geom GEOMETRY(POINT, 28992) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT CURRENT_USER
);

-- Indices voor performance
CREATE INDEX IF NOT EXISTS idx_brandweer_geom ON calamiteiten.brandweer_kazernes USING GIST(geom);
CREATE INDEX IF NOT EXISTS idx_brandweer_regio ON calamiteiten.brandweer_kazernes(veiligheidsregio);
CREATE INDEX IF NOT EXISTS idx_brandweer_type ON calamiteiten.brandweer_kazernes(type_kazerne);
CREATE INDEX IF NOT EXISTS idx_brandweer_plaats ON calamiteiten.brandweer_kazernes(plaats);
CREATE INDEX IF NOT EXISTS idx_brandweer_24_7 ON calamiteiten.brandweer_kazernes(bemanning_24_7) WHERE bemanning_24_7 = true;

-- Trigger voor updated_at
CREATE OR REPLACE FUNCTION calamiteiten.update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER brandweer_updated_at
    BEFORE UPDATE ON calamiteiten.brandweer_kazernes
    FOR EACH ROW
    EXECUTE FUNCTION calamiteiten.update_timestamp();

-- Comments
COMMENT ON TABLE calamiteiten.brandweer_kazernes IS 'Brandweerkazernes in Nederland voor calamiteitenbeheer';
COMMENT ON COLUMN calamiteiten.brandweer_kazernes.type_kazerne IS 'Beroeps (24/7), Vrijwillig (op oproep), of Gemengd';
COMMENT ON COLUMN calamiteiten.brandweer_kazernes.specialisaties IS 'Array van specialisaties zoals Hoogwerker, Duikteam, Chemisch';
COMMENT ON COLUMN calamiteiten.brandweer_kazernes.geom IS 'Locatie in RD New (EPSG:28992)';

-- =====================================================
-- Table: noodsteunpunten
-- =====================================================

CREATE TABLE IF NOT EXISTS calamiteiten.noodsteunpunten (
    id SERIAL PRIMARY KEY,
    naam VARCHAR(255) NOT NULL,
    adres VARCHAR(255),
    plaats VARCHAR(100),
    postcode VARCHAR(10),
    type_locatie VARCHAR(50) CHECK (type_locatie IN (
        'Sporthal', 'School', 'Gemeentehuis', 'Kerk', 
        'Ziekenhuis', 'Hotel', 'Kazerne', 'Anders'
    )),
    capaciteit INTEGER CHECK (capaciteit > 0),
    capaciteit_bedden INTEGER,
    faciliteiten TEXT, -- Comma separated: 'Bedden, Keuken, Sanitair, Medicijnen, Noodstroom'
    heeft_keuken BOOLEAN DEFAULT false,
    heeft_sanitair BOOLEAN DEFAULT false,
    heeft_noodstroom BOOLEAN DEFAULT false,
    heeft_ehbo BOOLEAN DEFAULT false,
    beschikbaarheid VARCHAR(20) CHECK (beschikbaarheid IN ('24/7', 'Op aanvraag', 'Daguren', 'Beperkt')),
    contactpersoon VARCHAR(100),
    telefoonnummer VARCHAR(20),
    email VARCHAR(100),
    toegankelijk_mindervaliden BOOLEAN DEFAULT false,
    parkeerplaatsen INTEGER,
    laatste_inspectie DATE,
    actief BOOLEAN DEFAULT true,
    opmerkingen TEXT,
    geom GEOMETRY(POINT, 28992) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT CURRENT_USER
);

-- Indices
CREATE INDEX IF NOT EXISTS idx_noodsteun_geom ON calamiteiten.noodsteunpunten USING GIST(geom);
CREATE INDEX IF NOT EXISTS idx_noodsteun_type ON calamiteiten.noodsteunpunten(type_locatie);
CREATE INDEX IF NOT EXISTS idx_noodsteun_plaats ON calamiteiten.noodsteunpunten(plaats);
CREATE INDEX IF NOT EXISTS idx_noodsteun_actief ON calamiteiten.noodsteunpunten(actief) WHERE actief = true;
CREATE INDEX IF NOT EXISTS idx_noodsteun_capaciteit ON calamiteiten.noodsteunpunten(capaciteit);

-- Trigger
CREATE TRIGGER noodsteun_updated_at
    BEFORE UPDATE ON calamiteiten.noodsteunpunten
    FOR EACH ROW
    EXECUTE FUNCTION calamiteiten.update_timestamp();

-- Comments
COMMENT ON TABLE calamiteiten.noodsteunpunten IS 'Noodopvang en evacuatielocaties voor calamiteiten';
COMMENT ON COLUMN calamiteiten.noodsteunpunten.capaciteit IS 'Maximum aantal personen';
COMMENT ON COLUMN calamiteiten.noodsteunpunten.beschikbaarheid IS 'Beschikbaarheid van de locatie';

-- =====================================================
-- Table: veiligheidsregios (referentie data)
-- =====================================================

CREATE TABLE IF NOT EXISTS calamiteiten.veiligheidsregios (
    id SERIAL PRIMARY KEY,
    naam VARCHAR(100) UNIQUE NOT NULL,
    code VARCHAR(10) UNIQUE,
    provincie VARCHAR(50),
    aantal_gemeenten INTEGER,
    inwoners INTEGER,
    oppervlakte_km2 DECIMAL(10,2),
    website VARCHAR(255),
    telefoonnummer VARCHAR(20),
    geom GEOMETRY(MULTIPOLYGON, 28992),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_veiligheidsregios_geom ON calamiteiten.veiligheidsregios USING GIST(geom);

COMMENT ON TABLE calamiteiten.veiligheidsregios IS '25 Veiligheidsregio''s in Nederland';

-- =====================================================
-- Views voor analyses
-- =====================================================

-- View: Brandweer met aantal noodsteunpunten in 5km
CREATE OR REPLACE VIEW calamiteiten.v_brandweer_dekking AS
SELECT 
    b.id,
    b.naam,
    b.plaats,
    b.veiligheidsregio,
    b.type_kazerne,
    b.aantal_voertuigen,
    b.geom,
    COUNT(n.id) as aantal_noodsteunpunten_binnen_5km,
    SUM(n.capaciteit) as totale_capaciteit_binnen_5km
FROM calamiteiten.brandweer_kazernes b
LEFT JOIN calamiteiten.noodsteunpunten n 
    ON ST_DWithin(b.geom, n.geom, 5000) -- 5km in meters
    AND n.actief = true
GROUP BY b.id, b.naam, b.plaats, b.veiligheidsregio, b.type_kazerne, b.aantal_voertuigen, b.geom;

COMMENT ON VIEW calamiteiten.v_brandweer_dekking IS 'Brandweerkazernes met aantal bereikbare noodsteunpunten binnen 5km';

-- View: Statistieken per veiligheidsregio
CREATE OR REPLACE VIEW calamiteiten.v_statistieken_per_regio AS
SELECT 
    b.veiligheidsregio,
    COUNT(DISTINCT b.id) as aantal_kazernes,
    SUM(b.aantal_voertuigen) as totaal_voertuigen,
    COUNT(DISTINCT b.id) FILTER (WHERE b.bemanning_24_7) as kazernes_24_7,
    COUNT(DISTINCT n.id) as aantal_noodsteunpunten,
    SUM(n.capaciteit) as totale_evacuatiecapaciteit,
    ROUND(AVG(n.capaciteit)::numeric, 0) as gemiddelde_capaciteit_per_punt
FROM calamiteiten.brandweer_kazernes b
LEFT JOIN calamiteiten.noodsteunpunten n 
    ON ST_DWithin(b.geom, n.geom, 10000) -- 10km
GROUP BY b.veiligheidsregio
ORDER BY aantal_kazernes DESC;

COMMENT ON VIEW calamiteiten.v_statistieken_per_regio IS 'Aggregaties per veiligheidsregio';

-- View: Noodsteunpunten zonder brandweer binnen 5km
CREATE OR REPLACE VIEW calamiteiten.v_noodsteun_zonder_dekking AS
SELECT 
    n.id,
    n.naam,
    n.adres,
    n.plaats,
    n.type_locatie,
    n.capaciteit,
    n.geom,
    ST_Distance(n.geom, b_nearest.geom) / 1000 as afstand_dichtstbijzijnde_kazerne_km
FROM calamiteiten.noodsteunpunten n
CROSS JOIN LATERAL (
    SELECT geom
    FROM calamiteiten.brandweer_kazernes
    ORDER BY n.geom <-> geom
    LIMIT 1
) b_nearest
WHERE NOT EXISTS (
    SELECT 1 
    FROM calamiteiten.brandweer_kazernes b
    WHERE ST_DWithin(n.geom, b.geom, 5000)
)
AND n.actief = true
ORDER BY afstand_dichtstbijzijnde_kazerne_km DESC;

COMMENT ON VIEW calamiteiten.v_noodsteun_zonder_dekking IS 'Noodsteunpunten buiten 5km bereik van brandweerkazerne';

-- =====================================================
-- Functions
-- =====================================================

-- Function: Vind dichtstbijzijnde brandweerkazerne
CREATE OR REPLACE FUNCTION calamiteiten.get_nearest_kazerne(point_geom GEOMETRY)
RETURNS TABLE (
    kazerne_naam VARCHAR,
    afstand_meter DOUBLE PRECISION,
    opkomsttijd_geschat INTEGER,
    geom GEOMETRY
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        b.naam,
        ST_Distance(point_geom, b.geom) as afstand,
        CASE 
            WHEN ST_Distance(point_geom, b.geom) < 1000 THEN 5
            WHEN ST_Distance(point_geom, b.geom) < 3000 THEN 10
            WHEN ST_Distance(point_geom, b.geom) < 5000 THEN 15
            ELSE 20
        END as opkomsttijd,
        b.geom
    FROM calamiteiten.brandweer_kazernes b
    ORDER BY point_geom <-> b.geom
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calamiteiten.get_nearest_kazerne IS 'Vind dichtstbijzijnde brandweerkazerne voor een punt';

-- Function: Bereken totale evacuatiecapaciteit binnen bereik
CREATE OR REPLACE FUNCTION calamiteiten.get_evacuatie_capaciteit(
    kazerne_id INTEGER,
    bereik_meters INTEGER DEFAULT 5000
)
RETURNS INTEGER AS $$
DECLARE
    totaal_capaciteit INTEGER;
BEGIN
    SELECT COALESCE(SUM(n.capaciteit), 0)
    INTO totaal_capaciteit
    FROM calamiteiten.noodsteunpunten n
    JOIN calamiteiten.brandweer_kazernes b ON b.id = kazerne_id
    WHERE ST_DWithin(b.geom, n.geom, bereik_meters)
    AND n.actief = true;
    
    RETURN totaal_capaciteit;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calamiteiten.get_evacuatie_capaciteit IS 'Bereken totale evacuatiecapaciteit binnen X meter van kazerne';

-- =====================================================
-- Sample Data
-- =====================================================

-- Insert sample brandweerkazernes
INSERT INTO calamiteiten.brandweer_kazernes 
(naam, adres, plaats, postcode, telefoonnummer, type_kazerne, veiligheidsregio, aantal_voertuigen, bemanning_24_7, specialisaties, opkomsttijd_minuten, geom)
VALUES 
-- Amsterdam
('Brandweer Amsterdam Centrum', 'Prins Hendrikkade 94', 'Amsterdam', '1012 AE', '020-5556666', 'Beroeps', 
 'Veiligheidsregio Amsterdam-Amstelland', 8, true, ARRAY['Hoogwerker', 'Duikteam'], 5,
 ST_SetSRID(ST_MakePoint(121632, 487473), 28992)),

('Brandweer Amsterdam Noord', 'Zonneplein 2', 'Amsterdam', '1033 HE', '020-5556667', 'Beroeps',
 'Veiligheidsregio Amsterdam-Amstelland', 6, true, ARRAY['Chemisch'], 7,
 ST_SetSRID(ST_MakePoint(121250, 490250), 28992)),

-- Rotterdam
('Brandweer Rotterdam Centrum', 'Schiekade 11', 'Rotterdam', '3032 AK', '010-4468000', 'Beroeps',
 'Veiligheidsregio Rotterdam-Rijnmond', 10, true, ARRAY['Hoogwerker', 'Havenbrand', 'Chemisch'], 5,
 ST_SetSRID(ST_MakePoint(92500, 437500), 28992)),

('Brandweer Rotterdam Noord', 'Schieweg 15', 'Rotterdam', '3038 BR', '010-4468001', 'Gemengd',
 'Veiligheidsregio Rotterdam-Rijnmond', 5, false, ARRAY['Duikteam'], 10,
 ST_SetSRID(ST_MakePoint(91750, 438850), 28992)),

-- Utrecht
('Brandweer Utrecht Centrum', 'Havenstraat 15', 'Utrecht', '3511 PK', '030-2864200', 'Beroeps',
 'Veiligheidsregio Utrecht', 6, true, ARRAY['Hoogwerker'], 6,
 ST_SetSRID(ST_MakePoint(136415, 455748), 28992)),

-- Den Haag
('Brandweer Den Haag Centrum', 'Fahrenheitstraat 640', 'Den Haag', '2561 DA', '070-3537000', 'Beroeps',
 'Veiligheidsregio Haaglanden', 9, true, ARRAY['Hoogwerker', 'Chemisch'], 5,
 ST_SetSRID(ST_MakePoint(81450, 454350), 28992)),

-- Groningen
('Brandweer Groningen', 'Ulgersmaweg 38', 'Groningen', '9731 BN', '050-3674500', 'Beroeps',
 'Veiligheidsregio Groningen', 7, true, ARRAY['Hoogwerker', 'Chemisch'], 7,
 ST_SetSRID(ST_MakePoint(233950, 582450), 28992))

ON CONFLICT DO NOTHING;

-- Insert sample noodsteunpunten
INSERT INTO calamiteiten.noodsteunpunten
(naam, adres, plaats, postcode, type_locatie, capaciteit, capaciteit_bedden, heeft_keuken, heeft_sanitair, heeft_noodstroom, heeft_ehbo, beschikbaarheid, contactpersoon, telefoonnummer, toegankelijk_mindervaliden, parkeerplaatsen, geom)
VALUES
-- Amsterdam
('Sporthal De Eendracht', 'Sportlaan 25', 'Amsterdam', '1012 AA', 'Sporthal', 500, 200, true, true, true, true, '24/7', 'J. de Vries', '020-1234567', true, 50,
 ST_SetSRID(ST_MakePoint(121500, 487500), 28992)),

('RAI Amsterdam', 'Europaplein 24', 'Amsterdam', '1078 GZ', 'Anders', 5000, 1000, true, true, true, true, 'Op aanvraag', 'Eventbeheer', '020-5491212', true, 500,
 ST_SetSRID(ST_MakePoint(121800, 482900), 28992)),

-- Rotterdam
('Sporthal Kralingen', 'Kralingseweg 200', 'Rotterdam', '3062 CG', 'Sporthal', 400, 150, true, true, true, true, '24/7', 'M. Peters', '010-4123456', true, 40,
 ST_SetSRID(ST_MakePoint(93500, 436000), 28992)),

('Ahoy Rotterdam', 'Ahoyweg 10', 'Rotterdam', '3084 BA', 'Anders', 10000, 2000, true, true, true, true, 'Op aanvraag', 'Ahoy Beheer', '010-2931300', true, 1000,
 ST_SetSRID(ST_MakePoint(90800, 435200), 28992)),

-- Utrecht
('Gemeentehuis Utrecht', 'Stadsplateau 1', 'Utrecht', '3521 AZ', 'Gemeentehuis', 300, 50, true, true, true, true, 'Daguren', 'M. Jansen', '030-2864000', true, 100,
 ST_SetSRID(ST_MakePoint(136400, 455700), 28992)),

('Sporthal Galgenwaard', 'Gageldijk 3', 'Utrecht', '3555 AX', 'Sporthal', 600, 250, true, true, true, true, '24/7', 'Sport Utrecht', '030-2881234', true, 75,
 ST_SetSRID(ST_MakePoint(137500, 455200), 28992)),

-- Den Haag
('Sportcampus Zuiderpark', 'Laan van Poot 1', 'Den Haag', '2566 EA', 'Sporthal', 800, 300, true, true, true, true, '24/7', 'Zuiderpark Beheer', '070-3537100', true, 100,
 ST_SetSRID(ST_MakePoint(80800, 451600), 28992)),

-- Groningen
('MartiniPlaza', 'Leonard Springerlaan 2', 'Groningen', '9727 KB', 'Anders', 3500, 700, true, true, true, true, 'Op aanvraag', 'MartiniPlaza BV', '050-5222222', true, 400,
 ST_SetSRID(ST_MakePoint(234800, 581900), 28992))

ON CONFLICT DO NOTHING;

-- Insert veiligheidsregio's
INSERT INTO calamiteiten.veiligheidsregios (naam, code, provincie, aantal_gemeenten, inwoners)
VALUES
('Veiligheidsregio Amsterdam-Amstelland', 'VRAA', 'Noord-Holland', 16, 1400000),
('Veiligheidsregio Rotterdam-Rijnmond', 'VRRR', 'Zuid-Holland', 15, 1300000),
('Veiligheidsregio Utrecht', 'VRU', 'Utrecht', 26, 1350000),
('Veiligheidsregio Haaglanden', 'VRH', 'Zuid-Holland', 8, 1050000),
('Veiligheidsregio Groningen', 'VRG', 'Groningen', 10, 590000)
ON CONFLICT (naam) DO NOTHING;

-- =====================================================
-- Grant permissions
-- =====================================================

-- FME user permissions
GRANT USAGE ON SCHEMA calamiteiten TO fmeuser;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA calamiteiten TO fmeuser;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA calamiteiten TO fmeuser;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA calamiteiten TO fmeuser;

-- Read-only user voor reporting
CREATE ROLE calamiteiten_readonly;
GRANT USAGE ON SCHEMA calamiteiten TO calamiteiten_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA calamiteiten TO calamiteiten_readonly;

-- =====================================================
-- Verification queries
-- =====================================================

-- Check data
SELECT 'Brandweerkazernes' as tabel, COUNT(*) as aantal FROM calamiteiten.brandweer_kazernes
UNION ALL
SELECT 'Noodsteunpunten', COUNT(*) FROM calamiteiten.noodsteunpunten
UNION ALL
SELECT 'Veiligheidsregio''s', COUNT(*) FROM calamiteiten.veiligheidsregios;

-- Check geometries
SELECT 
    'brandweer_kazernes' as tabel,
    COUNT(*) as totaal,
    COUNT(*) FILTER (WHERE ST_IsValid(geom)) as valide_geometrieen,
    COUNT(*) FILTER (WHERE NOT ST_IsValid(geom)) as invalide_geometrieen
FROM calamiteiten.brandweer_kazernes
UNION ALL
SELECT 
    'noodsteunpunten',
    COUNT(*),
    COUNT(*) FILTER (WHERE ST_IsValid(geom)),
    COUNT(*) FILTER (WHERE NOT ST_IsValid(geom))
FROM calamiteiten.noodsteunpunten;

-- Test views
SELECT * FROM calamiteiten.v_statistieken_per_regio;
SELECT * FROM calamiteiten.v_noodsteun_zonder_dekking LIMIT 5;

-- Test functions
SELECT * FROM calamiteiten.get_nearest_kazerne(ST_SetSRID(ST_MakePoint(121500, 487500), 28992));
SELECT calamiteiten.get_evacuatie_capaciteit(1, 5000) as capaciteit_binnen_5km;

-- Analyze tables
ANALYZE calamiteiten.brandweer_kazernes;
ANALYZE calamiteiten.noodsteunpunten;
ANALYZE calamiteiten.veiligheidsregios;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Database setup compleet!';
    RAISE NOTICE 'Schema: calamiteiten';
    RAISE NOTICE 'Tabellen: %, %, %', 
        (SELECT COUNT(*) FROM calamiteiten.brandweer_kazernes),
        (SELECT COUNT(*) FROM calamiteiten.noodsteunpunten),
        (SELECT COUNT(*) FROM calamiteiten.veiligheidsregios);
    RAISE NOTICE '==============================================';
END $$;
