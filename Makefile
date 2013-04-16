TURNIN_DIR = stage2_cgd2120_ewd2106
default: turnin

turnin: 
	mkdir $(TURNIN_DIR)
	cp -r src/ $(TURNIN_DIR)
	cp README $(TURNIN_DIR)
	cp query.txt $(TURNIN_DIR)
	cp config.txt $(TURNIN_DIR)
	cp output.txt $(TURNIN_DIR)
	tar cvzf $(TURNIN_DIR).tar.gz $(TURNIN_DIR)

clean:
	$(RM) -r $(TURNIN_DIR) $(TURNIN_DIR).tar.gz
