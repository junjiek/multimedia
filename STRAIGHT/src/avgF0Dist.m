function dist = avgF0Dist(filename1, filename2)
    x = avgF0(filename1);
    y = avgF0(filename2);
    dist = abs(x-y);
    fprintf(sprintf('(%.2f %.2f)  %.2f, %.2f\n', x, y, dist, y/x));
end

function res = avgF0(filename)
    [y, Fs] = wavread(filename);
    F0 = exstraightsource(y, Fs);
    res = mean(F0);
end