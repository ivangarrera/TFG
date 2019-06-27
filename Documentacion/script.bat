pdflatex -synctex=1 -interaction=nonstopmode main.tex > errores.txt
bibtex main.aux >> errores.txt
pdflatex -synctex=1 -interaction=nonstopmode main.tex >> errores.txt
pdflatex -synctex=1 -interaction=nonstopmode main.tex >> errores.txt

DEL main.aux main.synctex.gz main.toc main.log main.bbl main.blg 
main.lof main.lol main.lot

explorer.exe "main.pdf"
