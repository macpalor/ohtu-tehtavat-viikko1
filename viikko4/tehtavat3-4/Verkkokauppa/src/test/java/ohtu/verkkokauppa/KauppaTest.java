package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class KauppaTest {
    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;
    Kauppa kauppa;
    
    @Before
    public void setUp() {
        pankki = mock(Pankki.class);
        viite = mock(Viitegeneraattori.class);
        varasto = mock(Varasto.class);
        kauppa = new Kauppa(varasto, pankki, viite);
        kauppa.aloitaAsiointi();
    }

    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);
        
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        //Kauppa k = new Kauppa(varasto, pankki, viite);              

        // tehdään ostokset
        kauppa.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        kauppa.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(),anyInt());   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaanOikeillaParametreilla() {
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);
        
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        //Kauppa k = new Kauppa(varasto, pankki, viite);              

        // tehdään ostokset
        kauppa.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        kauppa.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(5));   
        // toistaiseksi ei välitetty kutsussa käytetyistä parametreista
    }
    
    @Test
    public void pankinMetodiaTilisiirtoKutsutaanOikeillaParametreillaKunOstetaanKahtaEriTuotetta() {
        when(viite.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(10); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "jaloviina", 15));
        
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(20));
    }
    
    @Test
    public void pankinMetodiaTilisiirtoKutsutaanOikeillaParametreillaKunOstetaanKahtaSamaaTuotetta() {
        when(viite.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(10));
    }
    
    @Test
    public void pankinMetodiaTilisiirtoKutsutaanOikeillaParametreillaKunOstetaanKahtaEriTuotettaJoistaToisenSaldoNolla() {
        when(viite.uusi()).thenReturn(42);
        when(varasto.saldo(1)).thenReturn(10); 
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        when(varasto.saldo(2)).thenReturn(0); 
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "jaloviina", 15));
        
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(2);
        kauppa.tilimaksu("pekka", "12345");
        
        verify(pankki).tilisiirto(eq("pekka"), eq(42), eq("12345"), eq("33333-44455"), eq(5));
    }
    
    @Test
    public void metodinAloitaAsiointiKutsuminenNollaaEdellisenOstoksenTiedot() {
       when(viite.uusi()).thenReturn(42);
       when(varasto.saldo(1)).thenReturn(10);
       when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
       
       kauppa.lisaaKoriin(1);
       kauppa.tilimaksu("pekka", "12345");
       verify(pankki, times(1)).tilisiirto(anyString(), anyInt(), anyString(), anyString(), anyInt());
       
       kauppa.aloitaAsiointi();
       kauppa.lisaaKoriin(1);
       kauppa.tilimaksu("erkki", "67890");
       verify(pankki, times(2)).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));
    }
    
    @Test
    public void uusiViitenumeroJokaiselleMaksutapahtumalle() {
        when(viite.uusi()).thenReturn(1).thenReturn(2).thenReturn(3);
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");
        verify(pankki).tilisiirto(anyString(), eq(1), anyString(), anyString(), anyInt());
        
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("erkki", "67890");
        verify(pankki).tilisiirto(anyString(), eq(2), anyString(), anyString(), anyInt());
        
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("eeva", "12321");
        verify(pankki).tilisiirto(anyString(), eq(3), anyString(), anyString(), anyInt());
        
    }
    
    @Test
    public void poistaKoristaMetodinKutsuTyhjentaaOstoskorin() {
        kauppa.lisaaKoriin(1);
        kauppa.poistaKorista(1);
        kauppa.tilimaksu("pekka", "12345");
        verify(pankki).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(0));
    }
}

