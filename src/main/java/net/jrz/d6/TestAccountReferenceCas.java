package net.jrz.d6;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j(topic = "s.TestAccountReferenceCas")
public class TestAccountReferenceCas {
    public static void main(String[] args) {
        DecimalAccountRefCas decimalAccountRefCas = new DecimalAccountRefCas(new AtomicReference<>(new BigDecimal(10000)));
        DecimalAccount.demo(decimalAccountRefCas);
    }
}

class DecimalAccountRefCas implements DecimalAccount{
    private AtomicReference<BigDecimal> ref;

    public DecimalAccountRefCas(AtomicReference<BigDecimal> ref) {
        this.ref = ref;
    }

    @Override
    public BigDecimal getBalance() {
        return ref.get();
    }

    @Override
    public void withdraw(BigDecimal amount) {
        while (true){
            BigDecimal prev = ref.get();
            BigDecimal cur = prev.subtract(amount);
            if (ref.compareAndSet(prev, cur)){
                break;
            }
        }
    }
}


