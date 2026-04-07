import java.util.*;
import java.util.stream.Collectors;

public class EvenimentService {
    private List<Eveniment> evenimente;
    private List<User> users;
    private List<Bilet> bilete;
    private List<Comanda> comenzi;
    private List<Recenzie> recenzii;
    private List<Tranzactie> tranzactii;
    private List<CodPromo> coduriPromo;

    public EvenimentService() {
        this.evenimente = new ArrayList<>();
        this.users = new ArrayList<>();
        this.bilete = new ArrayList<>();
        this.comenzi = new ArrayList<>();
        this.recenzii = new ArrayList<>();
        this.tranzactii = new ArrayList<>();
        this.coduriPromo = new ArrayList<>();
    }

    public void adaugaEveniment(Eveniment eveniment) {
        this.evenimente.add(eveniment);
    }
    public void stergeEveniment(int evenimentId) {
        this.evenimente.removeIf(e -> e.getId() == evenimentId);
    }
    public void modificaEveniment(int evenimentId, Eveniment evenimentNou) {
        for (int i = 0; i < this.evenimente.size(); i++) {
            if (this.evenimente.get(i).getId() == evenimentId) {
                this.evenimente.set(i, evenimentNou);
                break;
            }
        }
    }
    public Eveniment cautaEvenimentDupaId(int evenimentId) {
        return this.evenimente.stream()
                .filter(e -> e.getId() == evenimentId)
                .findFirst()
                .orElse(null);
    }

    public void cumparaBilet(User user, Eveniment eveniment, TipBilet tip, Comanda comanda) {
        Bilet bilet = new Bilet(this.bilete.size() + 1, eveniment, user, tip);
        this.bilete.add(bilet);
        comanda.addBilet(bilet);
        double pretBilet = eveniment.getPret();
         switch (tip) {
            case VIP:
                pretBilet *= 1.5;
                break;
            case STANDARD:
                break;
            case STUDENT:
                pretBilet *= 0.8;
                break;
            case EARLY_BIRD:
                pretBilet *= 0.7;
                break;
        }   
        if (comanda.getCodPromo() != null) {
            pretBilet = aplicaCodPromo(comanda, comanda.getCodPromo().getCod());
        }
        user.setBalanta(user.getBalanta() - pretBilet);
        comanda.setTranzactie(new Tranzactie(this.tranzactii.size() + 1, comanda, pretBilet, new Date()));
        comanda.setStatus("finalizata");
        this.comenzi.add(comanda);

    }

    public void refundBilet(int biletId) {
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);
        if (bilet != null) {
            Comanda comanda = bilet.getComanda();
            double suma = comanda.getTranzactie().getSuma();
            User user = bilet.getUser();
            user.setBalanta(user.getBalanta() + suma);
            comanda.setStatus("refundata");
            this.bilete.remove(bilet);
        }
    }

    public List<Comanda> getIstoricComenziUser(int userId) {
        User user = this.users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);
        if(!user.getComenzi().isEmpty()) {
            return user.getComenzi();
        }
        return null;
    }

    public List<Eveniment> cautaEvenimentDupaNume(String nume) {
        return null;
    }
    public List<Eveniment> filtreazaEvenimenteDupaTip(String tipEveniment) {
        return null;
    }
    public List<Eveniment> filtreazaEvenimenteDupaLocatie(int locatieId) {
        return null;
    }
    public List<Eveniment> filtreazaEvenimenteDupaData(Date dataStart, Date dataEnd) {
        return null;
    }

    public double raportVanzariPerLocatie(int locatieId) {
        return 0.0;
    }
    public double raportVanzariPerEveniment(int evenimentId) {
        return 0.0;
    }
    public int countBileteVandute(int evenimentId) {
        return 0;
    }

    public double aplicaCodPromo(Comanda comanda, String codPromo) {
        return 0.0;
    }

    public void transferBilet(int biletId, User userNou) {}

    public void adaugaRecenzie(User user, Eveniment eveniment, int nota, String comentariu) {}
    public List<Recenzie> getRecenziiEveniment(int evenimentId) {
        return null;
    }

    public void exportareBiletePDF(int evenimentId, String filePath) {}
    public void exportareBileteTXT(int evenimentId, String filePath) {}

    public void adaugaUser(User user) {}
    public void adaugaCodPromo(CodPromo cod) {}
    public List<Eveniment> getToateEvenimentele() {
        return null;
    }
    public double getTotalVanzariComanda(int comandaId) {
        return 0.0;
    }
    public double getAverageRatingEveniment(int evenimentId) {
        return 0.0;
    }
}
